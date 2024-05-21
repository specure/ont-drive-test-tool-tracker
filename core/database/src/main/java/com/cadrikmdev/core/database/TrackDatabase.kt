package com.cadrikmdev.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cadrikmdev.core.database.dao.TrackDao
import com.cadrikmdev.core.database.dao.TrackPendingSyncDao
import com.cadrikmdev.core.database.entity.DeleteTrackSyncEntity
import com.cadrikmdev.core.database.entity.TrackEntity
import com.cadrikmdev.core.database.entity.TrackPendingSyncEntity

@Database(
    entities = [
        TrackEntity::class,
        TrackPendingSyncEntity::class,
        DeleteTrackSyncEntity::class,
    ],
    version = 1
)
abstract class TrackDatabase : RoomDatabase() {

    abstract val trackDao: TrackDao
    abstract val trackPendingSyncDao: TrackPendingSyncDao

}