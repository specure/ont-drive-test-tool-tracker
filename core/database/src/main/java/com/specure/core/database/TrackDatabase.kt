package com.specure.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.specure.core.database.dao.TrackDao
import com.specure.core.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
    ],
    version = 5
)
abstract class TrackDatabase : RoomDatabase() {

    abstract val trackDao: TrackDao

}