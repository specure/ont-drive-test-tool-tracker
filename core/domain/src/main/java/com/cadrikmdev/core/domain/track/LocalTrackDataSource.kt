package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

typealias TrackId = Long?

interface LocalTrackDataSource {
    fun getTracks(): Flow<List<Track>>

    suspend fun upsertTrack(track: Track): Result<TrackId, DataError.Local>

    suspend fun upsertTracks(tracks: List<Track>): Result<List<TrackId>, DataError.Local>

    suspend fun getTracksForExport(): Flow<List<Track>>

    suspend fun getLatestNTracksForExport(limit: Int): Flow<List<Track>>

    suspend fun deleteExportedTracks()

    suspend fun deleteTrack(id: TrackId)

    suspend fun deleteAllTracks()
}