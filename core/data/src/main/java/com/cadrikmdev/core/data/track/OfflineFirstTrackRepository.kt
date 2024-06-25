package com.cadrikmdev.core.data.track

import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.track.TrackId
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import com.cadrikmdev.core.domain.util.Result
import com.cadrikmdev.core.domain.util.asEmptyDataResult
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

    override suspend fun exportPendingTracks() {
        // TODO: export it to CSV file
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