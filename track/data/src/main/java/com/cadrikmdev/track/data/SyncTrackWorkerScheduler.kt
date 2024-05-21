package com.cadrikmdev.track.data

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.cadrikmdev.core.database.dao.TrackPendingSyncDao
import com.cadrikmdev.core.database.entity.DeleteTrackSyncEntity
import com.cadrikmdev.core.database.entity.TrackPendingSyncEntity
import com.cadrikmdev.core.database.mappers.toTrackEntity
import com.cadrikmdev.core.domain.SessionStorage
import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.track.TrackId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class SyncTrackWorkerScheduler(
    private val context: Context,
    private val pendingSyncDao: TrackPendingSyncDao,
    private val sessionStorage: SessionStorage,
    private val applicationScope: CoroutineScope,
) : SyncTrackScheduler {

    private val workManager = WorkManager.getInstance(context)
    override suspend fun scheduleSync(type: SyncTrackScheduler.SyncType) {
        when (type) {
            is SyncTrackScheduler.SyncType.CreateTracks -> {
                scheduleCreateTrackWorker(
                    track = type.track,
                )
            }

            is SyncTrackScheduler.SyncType.DeleteTracks -> scheduleDeleteTrackWorker(type.trackId)
            is SyncTrackScheduler.SyncType.FetchTracks -> {
                scheduleFetchTracksWorker(type.interval)
            }
        }
    }

    private suspend fun scheduleDeleteTrackWorker(trackId: TrackId) {
        val userId = sessionStorage.get()?.userId ?: return
        val entity = DeleteTrackSyncEntity(
            trackId = trackId,
            userId = userId
        )
        pendingSyncDao.upsertDeletedTrackSyncEntity(entity)

        val workRequest = OneTimeWorkRequestBuilder<DeleteTrackWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS
            )
            .setInputData(
                Data.Builder()
                    .putString(DeleteTrackWorker.TRACK_ID, entity.trackId)
                    .build()
            )
            .addTag(DELETE_TRACK_WORKER_TAG)
            .build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleCreateTrackWorker(track: Track) {
        val userId = sessionStorage.get()?.userId ?: return
        val pendingTrack = TrackPendingSyncEntity(
            track = track.toTrackEntity(),
            userId = userId
        )
        pendingSyncDao.upsertTrackPendingSyncEntity(pendingTrack)

        val workRequest = OneTimeWorkRequestBuilder<CreateTrackWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS
            )
            .setInputData(
                Data.Builder()
                    .putString(CreateTrackWorker.TRACK_ID, pendingTrack.trackId)
                    .build()
            )
            .addTag(CREATE_TRACK_WORKER_TAG)
            .build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleFetchTracksWorker(interval: Duration) {
        val isSyncScheduled = withContext(Dispatchers.IO) {
            workManager
                .getWorkInfosByTag(FETCH_TRACK_WORKER_TAG)
                .get()
                .isNotEmpty()
        }
        if (isSyncScheduled) {
            return
        }
        val workRequest = PeriodicWorkRequestBuilder<FetchTracksWorker>(
            repeatInterval = interval.toJavaDuration()
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                backoffDelay = 2000L,
                timeUnit = TimeUnit.MILLISECONDS
            )
            .setInitialDelay(
                duration = 30,
                timeUnit = TimeUnit.MINUTES
            )
            .addTag(FETCH_TRACK_WORKER_TAG)
            .build()
        workManager.enqueue(workRequest).await()
    }

    override suspend fun cancelAllSyncs() {
        workManager
            .cancelAllWork()
            .await()
    }

    companion object {
        private const val FETCH_TRACK_WORKER_TAG = "sync_work"
        private const val CREATE_TRACK_WORKER_TAG = "create_work"
        private const val DELETE_TRACK_WORKER_TAG = "delete_work"
    }
}