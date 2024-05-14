package com.cadrikmdev.core.database

import android.database.sqlite.SQLiteFullException
import com.cadrikmdev.core.database.dao.RunDao
import com.cadrikmdev.core.database.mappers.toRun
import com.cadrikmdev.core.database.mappers.toRunEntity
import com.cadrikmdev.core.domain.run.LocalRunDataSource
import com.cadrikmdev.core.domain.run.Run
import com.cadrikmdev.core.domain.run.RunId
import com.cadrikmdev.core.domain.util.DataError
import com.cadrikmdev.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLocalRunDataSource(
    private val runDao: RunDao
) : LocalRunDataSource {
    override fun getRuns(): Flow<List<Run>> {
        return runDao.getRuns()
            .map { runEntities ->
                runEntities.map {
                    it.toRun()
                }
            }
    }

    override suspend fun upsertRun(run: Run): Result<RunId, DataError.Local> {
        return try {
            val entity = run.toRunEntity()
            runDao.upsertRun(entity)
            Result.Success(entity.id)
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun upsertRuns(runs: List<Run>): Result<List<RunId>, DataError.Local> {
        return try {
            val entities = runs.map {
                it.toRunEntity()
            }
            runDao.upsertRuns(entities)
            Result.Success(entities.map { it.id })
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }

    override suspend fun deleteRun(id: String) {
        runDao.deleteRun(id)
    }

    override suspend fun deleteAllRuns() {
        runDao.deleteAllRuns()
    }
}