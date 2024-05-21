package com.cadrikmdev.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cadrikmdev.core.database.entity.DeleteTrackSyncEntity
import com.cadrikmdev.core.database.entity.TrackPendingSyncEntity

@Dao
interface TrackPendingSyncDao {

    // CREATED RUNS
    @Query("SELECT * FROM trackpendingsyncentity WHERE userId=:userId")
    suspend fun getAllTrackPendingSyncEntities(userId: String): List<TrackPendingSyncEntity>

    @Query("SELECT * FROM trackpendingsyncentity WHERE trackId=:trackId")
    suspend fun getTrackPendingSyncEntity(trackId: String): TrackPendingSyncEntity?

    @Upsert
    suspend fun upsertTrackPendingSyncEntity(entity: TrackPendingSyncEntity)

    @Query("DELETE FROM trackpendingsyncentity WHERE trackId=:trackId")
    suspend fun deleteTrackPendingSyncEntity(trackId: String)

    // DELETED RUNS
    @Query("SELECT * FROM deletetracksyncentity WHERE userId=:userId")
    suspend fun getAllDeletedTrackSyncEntities(userId: String): List<DeleteTrackSyncEntity>

    @Upsert
    suspend fun upsertDeletedTrackSyncEntity(entity: DeleteTrackSyncEntity)

    @Query("DELETE FROM deletetracksyncentity WHERE trackId=:trackId")
    suspend fun deleteDeletedTrackSyncEntity(trackId: String)
}