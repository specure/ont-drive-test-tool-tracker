package com.cadrikmdev.run.data.di

import com.cadrikmdev.core.domain.run.SyncRunScheduler
import com.cadrikmdev.run.data.CreateRunWorker
import com.cadrikmdev.run.data.DeleteRunWorker
import com.cadrikmdev.run.data.FetchRunsWorker
import com.cadrikmdev.run.data.SyncRunWorkerScheduler
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val runDataModule = module {
    workerOf(::CreateRunWorker)
    workerOf(::FetchRunsWorker)
    workerOf(::DeleteRunWorker)

    singleOf(::SyncRunWorkerScheduler).bind<SyncRunScheduler>()
}