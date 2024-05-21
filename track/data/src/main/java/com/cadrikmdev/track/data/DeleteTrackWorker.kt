package com.cadrikmdev.track.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.database.dao.TrackPendingSyncDao
import com.cadrikmdev.core.domain.track.RemoteTrackDataSource

class DeleteTrackWorker(
    context: Context,
    private val parameters: WorkerParameters,
    private val remoteTrackDataSource: RemoteTrackDataSource,
    private val pendingSyncDao: TrackPendingSyncDao,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        val trackId = parameters.inputData.getString(TRACK_ID) ?: return Result.failure()

        return when (val result = remoteTrackDataSource.deleteTrack(trackId)) {
            is com.cadrikmdev.core.domain.util.Result.Error -> {
                result.error.toWorkerResult()
            }

            is com.cadrikmdev.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteDeletedTrackSyncEntity(trackId)
                Result.success()
            }
        }
    }

    companion object {
        const val TRACK_ID = "TRACK_ID"
    }
}