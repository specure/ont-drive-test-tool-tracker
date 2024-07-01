package com.cadrikmdev.core.database.export

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import kotlinx.coroutines.flow.first
import timber.log.Timber

class MarkAsExportedWorker(
    context: Context,
    parameters: WorkerParameters,
    private val localTrackDataSource: LocalTrackDataSource,
) : CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }

        val tracks = localTrackDataSource.getTracksForExport().first()
        var workerResult: Result = Result.success()

        Timber.d("mark as exported ${tracks.size}")
        tracks.forEach {
            it.exported = true
            val result = localTrackDataSource.upsertTrack(it)
            if (result is com.cadrikmdev.core.domain.util.Result.Error) {
                workerResult = result.error.toWorkerResult()
            }
        }
        return workerResult
    }
}