package com.cadrikmdev.run.data.di

import com.cadrikmdev.run.data.CreateRunWorker
import com.cadrikmdev.run.data.DeleteRunWorker
import com.cadrikmdev.run.data.FetchRunsWorker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val runDataModule = module {
    workerOf(::CreateRunWorker)
    workerOf(::FetchRunsWorker)
    workerOf(::DeleteRunWorker)
}