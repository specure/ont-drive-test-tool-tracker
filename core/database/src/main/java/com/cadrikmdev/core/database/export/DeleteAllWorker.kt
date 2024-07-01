package com.cadrikmdev.core.database.export

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.domain.track.LocalTrackDataSource

class DeleteAllWorker(
    context: Context,
    parameters: WorkerParameters,
    private val localTrackDataSource: LocalTrackDataSource,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        localTrackDataSource.deleteAllTracks()
        return Result.success()
    }
}