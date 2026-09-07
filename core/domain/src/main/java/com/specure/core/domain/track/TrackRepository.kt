package com.specure.core.domain.track

import com.specure.core.domain.util.DataError
import com.specure.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    suspend fun upsertTrack(track: Track): EmptyResult<DataError>

    suspend fun deleteTrack(id: TrackId)

    suspend fun getTracksForExportCount(): Flow<Int>

    suspend fun deleteExportedTracks()

    suspend fun deleteAllTracks()

}