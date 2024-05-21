package com.cadrikmdev.track.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.domain.track.TrackRepository

class FetchTracksWorker(
    context: Context,
    params: WorkerParameters,
    private val trackRepository: TrackRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        return when (val result = trackRepository.fetchTracks()) {
            is com.cadrikmdev.core.domain.util.Result.Error -> {
                result.error.toWorkerResult()
            }

            is com.cadrikmdev.core.domain.util.Result.Success -> Result.success()
        }
    }
}