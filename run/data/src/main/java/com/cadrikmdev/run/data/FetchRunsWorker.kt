package com.cadrikmdev.run.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.domain.run.RunRepository
import com.cadrikmdev.core.domain.util.Result

class FetchRunsWorker(
    context: Context,
    params: WorkerParameters,
    private val runRepository: RunRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        return when (val result = runRepository.fetchRuns()) {
            is com.cadrikmdev.core.domain.util.Result.Error -> {
                result.error.toWorkerResult()
            }

            is com.cadrikmdev.core.domain.util.Result.Success -> Result.success()
        }
    }
}