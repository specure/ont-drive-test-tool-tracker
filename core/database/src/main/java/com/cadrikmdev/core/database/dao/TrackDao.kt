package com.cadrikmdev.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cadrikmdev.core.database.entity.TrackEntity
import com.cadrikmdev.core.domain.track.TrackId
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsertTrack(run: TrackEntity)

    @Upsert
    suspend fun upsertTracks(runs: List<TrackEntity>)

    @Query("SELECT * FROM track_entity ORDER BY timestamp DESC")
    fun getTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track_entity WHERE exported=0  ORDER BY timestamp DESC")
    fun getTracksForExport(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track_entity WHERE exported=0 ORDER BY timestamp DESC LIMIT :limit")
    fun getLastNTracksForExport(limit: Int): Flow<List<TrackEntity>>

    @Query("DELETE FROM track_entity WHERE exported=1")
    suspend fun deleteExportedTracks()

    @Query("DELETE FROM track_entity WHERE id=:id")
    suspend fun deleteTrack(id: TrackId)

    @Query("DELETE FROM track_entity")
    suspend fun deleteAllTracks()
}