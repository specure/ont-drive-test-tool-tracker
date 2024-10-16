package com.specure.core.data.track

import com.specure.core.domain.track.LocalTrackDataSource
import com.specure.core.domain.track.Track
import com.specure.core.domain.track.TrackId
import com.specure.core.domain.track.TrackRepository
import com.specure.core.domain.util.DataError
import com.specure.core.domain.util.EmptyResult
import com.specure.core.domain.util.Result
import com.specure.core.domain.util.asEmptyDataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OfflineFirstTrackRepository(
    private val localTrackDataSource: LocalTrackDataSource,
) : TrackRepository {
    override fun getTracks(): Flow<List<Track>> {
        return localTrackDataSource.getTracks()
    }

    override suspend fun upsertTrack(track: Track): EmptyResult<DataError> {
        val localResult = localTrackDataSource.upsertTrack(track)
        if (localResult !is Result.Success) {
            return localResult.asEmptyDataResult()
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteTrack(id: TrackId) {
        localTrackDataSource.deleteTrack(id)
    }

    override suspend fun getTracksForExport(): Flow<List<Track>> {
        return localTrackDataSource.getTracksForExport()
    }

    override suspend fun deleteExportedTracks() {
        withContext(Dispatchers.IO) {
            val deletedTracks = async {
                localTrackDataSource.deleteExportedTracks()
            }
            deletedTracks.await()
        }
    }

    override suspend fun deleteAllTracks() {
        localTrackDataSource.deleteAllTracks()
    }
}