package com.cadrikmdev.core.data.track

import com.cadrikmdev.core.database.dao.TrackPendingSyncDao
import com.cadrikmdev.core.database.mappers.toTrack
import com.cadrikmdev.core.domain.SessionStorage
import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import com.cadrikmdev.core.domain.track.RemoteTrackDataSource
import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.track.TrackId
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import com.cadrikmdev.core.domain.util.Result
import com.cadrikmdev.core.domain.util.asEmptyDataResult
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineFirstTrackRepository(
    private val localTrackDataSource: LocalTrackDataSource,
    private val remoteTrackDataSource: RemoteTrackDataSource,
    private val applicationScope: CoroutineScope,
    private val trackPendingSyncDao: TrackPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val syncTrackScheduler: SyncTrackScheduler,
    private val httpClient: HttpClient,
) : TrackRepository {
    override fun getTracks(): Flow<List<Track>> {
        return localTrackDataSource.getTracks()
    }

    override suspend fun fetchTracks(): EmptyResult<DataError> {
        return when (val result = remoteTrackDataSource.getTracks()) {
            is Result.Error -> result.asEmptyDataResult()
            is Result.Success -> {
                // to keep database updated in case coroutine has been cancelled, right after getting results and starting to update DB
                applicationScope.async {
                    localTrackDataSource.upsertTracks(result.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun upsertTrack(track: Track): EmptyResult<DataError> {
        val localResult = localTrackDataSource.upsertTrack(track)
        if (localResult !is Result.Success) {
            return localResult.asEmptyDataResult()
        }
        val trackWithId = track.copy(id = localResult.data)
        val remoteResult = remoteTrackDataSource.postTrack(
            track = trackWithId,
        )
        return when (remoteResult) {
            is Result.Error -> {
                applicationScope.launch {
                    syncTrackScheduler.scheduleSync(
                        type = SyncTrackScheduler.SyncType.CreateTracks(
                            track = trackWithId,
                        )
                    )
                }.join()
                Result.Success(Unit)
            }

            is Result.Success -> {
                applicationScope.async {
                    localTrackDataSource.upsertTrack(remoteResult.data).asEmptyDataResult()
                }.await()
            }
        }
    }

    override suspend fun deleteTrack(id: TrackId) {
        localTrackDataSource.deleteTrack(id)

        // Edge case where the track is created in offline mode,
        // and then deleted in offline mode as well,
        // in that case we do not need to sync anything
        val isPendingSync = trackPendingSyncDao.getTrackPendingSyncEntity(id) != null
        if (isPendingSync) {
            trackPendingSyncDao.deleteTrackPendingSyncEntity(id)
            return
        }

        val remoteResult = applicationScope.async {
            remoteTrackDataSource.deleteTrack(id)
        }.await()

        if (remoteResult is Result.Error) {
            applicationScope.launch {
                syncTrackScheduler.scheduleSync(
                    type = SyncTrackScheduler.SyncType.DeleteTracks(id)
                )
            }.join()
        }
    }

    override suspend fun syncPendingTracks() {
        withContext(Dispatchers.IO) {
            val userId = sessionStorage.get()?.userId ?: return@withContext

            val createdTracks = async {
                trackPendingSyncDao.getAllTrackPendingSyncEntities(userId)
            }
            val deletedTracks = async {
                trackPendingSyncDao.getAllDeletedTrackSyncEntities(userId)
            }
            val createJobs = createdTracks
                .await()
                .map {
                    launch {
                        val track = it.track.toTrack()
                        trackPendingSyncDao.deleteTrackPendingSyncEntity(it.trackId)
                    }
                }
            val deleteJobs = deletedTracks
                .await()
                .map {
                    launch {
                        when (remoteTrackDataSource.deleteTrack(it.trackId)) {
                            is Result.Error -> Unit
                            is Result.Success -> {
                                applicationScope.launch {
                                    trackPendingSyncDao.deleteDeletedTrackSyncEntity(it.trackId)
                                }.join()
                            }
                        }
                    }
                }
            createJobs.forEach {
                it.join()
            }
            deleteJobs.forEach {
                it.join()
            }
        }
    }

    override suspend fun deleteAllTracks() {
        localTrackDataSource.deleteAllTracks()
    }
}