package com.cadrikmdev.core.database.di

import android.app.NotificationManager
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.room.Room
import com.cadrikmdev.core.database.RoomLocalTrackDataSource
import com.cadrikmdev.core.database.TrackDatabase
import com.cadrikmdev.core.database.export.CoreFileProvider
import com.cadrikmdev.core.database.export.DatabaseWorkerScheduler
import com.cadrikmdev.core.database.export.DeleteAllWorker
import com.cadrikmdev.core.database.export.DeleteExportedWorker
import com.cadrikmdev.core.database.export.MarkAsExportedWorker
import com.cadrikmdev.core.database.export.TracksExporter
import com.cadrikmdev.core.database.export.WorkerScheduler
import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            TrackDatabase::class.java,
            "run.db"
        ).build()
    }

    workerOf(::MarkAsExportedWorker)
    workerOf(::DeleteExportedWorker)
    workerOf(::DeleteAllWorker)

    singleOf(::RoomLocalTrackDataSource).bind<LocalTrackDataSource>()
    singleOf(::CoreFileProvider).bind<FileProvider>()
    singleOf(::DatabaseWorkerScheduler).bind<WorkerScheduler>()

    single { get<TrackDatabase>().trackDao }
    single { androidApplication().getSystemService<NotificationManager>() }
    single {
        TracksExporter(
            get(),
            get(),
            get(),
            get()
        )
    }

}