package com.cadrikmdev.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cadrikmdev.core.database.dao.TrackDao
import com.cadrikmdev.core.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
    ],
    version = 4
)
abstract class TrackDatabase : RoomDatabase() {

    abstract val trackDao: TrackDao

}