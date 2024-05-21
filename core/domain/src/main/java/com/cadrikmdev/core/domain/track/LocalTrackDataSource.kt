package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

typealias TrackId = String

interface LocalTrackDataSource {
    fun getTracks(): Flow<List<Track>>

    suspend fun upsertTrack(track: Track): Result<TrackId, DataError.Local>

    suspend fun upsertTracks(tracks: List<Track>): Result<List<TrackId>, DataError.Local>

    suspend fun deleteTrack(id: String)

    suspend fun deleteAllTracks()
}