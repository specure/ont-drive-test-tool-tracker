package com.cadrikmdev.track.data.di

import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.track.data.CreateTrackWorker
import com.cadrikmdev.track.data.DeleteTrackWorker
import com.cadrikmdev.track.data.FetchTracksWorker
import com.cadrikmdev.track.data.SyncTrackWorkerScheduler
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val trackDataModule = module {
    workerOf(::CreateTrackWorker)
    workerOf(::FetchTracksWorker)
    workerOf(::DeleteTrackWorker)

    singleOf(::SyncTrackWorkerScheduler).bind<SyncTrackScheduler>()
}