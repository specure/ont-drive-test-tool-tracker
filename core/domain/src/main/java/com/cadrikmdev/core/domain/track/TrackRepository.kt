package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.EmptyResult
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun getTracks(): Flow<List<Track>>

    suspend fun fetchTracks(): EmptyResult<DataError>

    suspend fun upsertTrack(track: Track): EmptyResult<DataError>

    suspend fun deleteTrack(id: TrackId)

    suspend fun syncPendingTracks()

    suspend fun deleteAllTracks()

}