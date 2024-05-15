package com.cadrikmdev.run.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.database.dao.RunPendingSyncDao
import com.cadrikmdev.core.database.mappers.toRun
import com.cadrikmdev.core.domain.run.RemoteRunDataSource

class CreateRunWorker(
    context: Context,
    private val parameters: WorkerParameters,
    private val remoteRunDataSource: RemoteRunDataSource,
    private val pendingSyncDao: RunPendingSyncDao,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        val pendingRunId = parameters.inputData.getString(RUN_ID) ?: return Result.failure()
        val pendingRunEntity = pendingSyncDao.getRunPendingSyncEntity(pendingRunId) ?: return Result.failure()

        val run = pendingRunEntity.run.toRun()

        return when (val result = remoteRunDataSource.postRun(run, pendingRunEntity.mapPictureBytes)) {
            is com.cadrikmdev.core.domain.util.Result.Error -> {
                result.error.toWorkerResult()
            }

            is com.cadrikmdev.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteRunPendingSyncEntity(pendingRunId)
                Result.success()
            }
        }
    }

    companion object {
        const val RUN_ID = "RUN_ID"
    }
}