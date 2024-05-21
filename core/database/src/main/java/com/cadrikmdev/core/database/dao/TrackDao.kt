package com.cadrikmdev.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cadrikmdev.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsertTrack(run: TrackEntity)

    @Upsert
    suspend fun upsertTracks(runs: List<TrackEntity>)

    @Query("SELECT * FROM trackentity ORDER BY dateTimeUtc DESC")
    fun getTracks(): Flow<List<TrackEntity>>

    @Query("DELETE FROM trackentity WHERE id=:id")
    suspend fun deleteTrack(id: String)

    @Query("DELETE FROM trackentity")
    suspend fun deleteAllTracks()
}