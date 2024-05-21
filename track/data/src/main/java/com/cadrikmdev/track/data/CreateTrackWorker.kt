package com.cadrikmdev.track.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cadrikmdev.core.database.dao.TrackPendingSyncDao
import com.cadrikmdev.core.database.mappers.toTrack
import com.cadrikmdev.core.domain.track.RemoteTrackDataSource

class CreateTrackWorker(
    context: Context,
    private val parameters: WorkerParameters,
    private val remoteTrackDataSource: RemoteTrackDataSource,
    private val pendingSyncDao: TrackPendingSyncDao,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= 5) {
            return Result.failure()
        }
        val pendingTrackId = parameters.inputData.getString(TRACK_ID) ?: return Result.failure()
        val pendingTrackEntity = pendingSyncDao.getTrackPendingSyncEntity(pendingTrackId) ?: return Result.failure()

        val track = pendingTrackEntity.track.toTrack()

        return when (val result = remoteTrackDataSource.postTrack(track)) {
            is com.cadrikmdev.core.domain.util.Result.Error -> {
                result.error.toWorkerResult()
            }

            is com.cadrikmdev.core.domain.util.Result.Success -> {
                pendingSyncDao.deleteTrackPendingSyncEntity(pendingTrackId)
                Result.success()
            }
        }
    }

    companion object {
        const val TRACK_ID = "TRACK_ID"
    }
}