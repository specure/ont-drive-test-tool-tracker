package com.cadrikmdev.core.database.export

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class DatabaseWorkerScheduler(
    context: Context,
    private val applicationScope: CoroutineScope,
) : WorkerScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleWork(type: WorkType) {
        when (type) {
            WorkType.DeleteAll -> {
                scheduleDeleteAllWorker()
            }

            WorkType.DeleteExported -> {
                scheduleDeleteExportedWorker()
            }

            WorkType.MarkAsExported -> {
                scheduleMarkAsExportedWorker()
            }
        }
    }

    override suspend fun cancelAllWork() {
        workManager
            .cancelAllWork()
            .await()
    }

    private suspend fun scheduleDeleteAllWorker() {
        Timber.d("Delete all worker enqueued")
        val workRequest = OneTimeWorkRequestBuilder<DeleteAllWorker>()
            .addTag(DELETE_ALL_WORKER_TAG)
            .build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleDeleteExportedWorker() {
        Timber.d("Delete exported worker enqueued")
        val workRequest = OneTimeWorkRequestBuilder<DeleteExportedWorker>()
            .addTag(DELETE_EXPORTED_WORKER_TAG)
            .build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    private suspend fun scheduleMarkAsExportedWorker() {
        Timber.d("Mark as exported worker enqueued")
        val workRequest = OneTimeWorkRequestBuilder<MarkAsExportedWorker>()
            .addTag(MARK_AS_EXPORTED_WORKER_TAG)
            .build()
        applicationScope.launch {
            workManager.enqueue(workRequest).await()
        }.join()
    }

    companion object {
        private const val DELETE_ALL_WORKER_TAG = "delete_all_tracks"
        private const val DELETE_EXPORTED_WORKER_TAG = "delete_exported_tracks"
        private const val MARK_AS_EXPORTED_WORKER_TAG = "mark_as_exported_tracks"
    }
}