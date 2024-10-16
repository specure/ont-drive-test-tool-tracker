package com.specure.core.database.export

interface WorkerScheduler {
    suspend fun scheduleWork(type: WorkType)

    suspend fun cancelAllWork()
}

sealed interface WorkType {
    data object MarkAsExported : WorkType
    data object DeleteExported : WorkType
    data object DeleteAll : WorkType
}