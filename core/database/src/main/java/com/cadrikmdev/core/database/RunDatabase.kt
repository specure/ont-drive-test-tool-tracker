package com.cadrikmdev.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cadrikmdev.core.database.dao.AnalyticsDao
import com.cadrikmdev.core.database.dao.RunDao
import com.cadrikmdev.core.database.dao.RunPendingSyncDao
import com.cadrikmdev.core.database.entity.DeleteRunSyncEntity
import com.cadrikmdev.core.database.entity.RunEntity
import com.cadrikmdev.core.database.entity.RunPendingSyncEntity

@Database(
    entities = [
        RunEntity::class,
        RunPendingSyncEntity::class,
        DeleteRunSyncEntity::class,
    ],
    version = 1
)
abstract class RunDatabase : RoomDatabase() {

    abstract val analyticsDao: AnalyticsDao
    abstract val runDao: RunDao
    abstract val runPendingSyncDao: RunPendingSyncDao

}