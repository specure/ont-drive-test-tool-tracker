package com.cadrikmdev.core.database.di

import android.app.NotificationManager
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cadrikmdev.core.database.RoomLocalTrackDataSource
import com.cadrikmdev.core.database.Tables
import com.cadrikmdev.core.database.TrackDatabase
import com.cadrikmdev.core.database.export.CoreFileProvider
import com.cadrikmdev.core.database.export.DatabaseManager
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

const val TRACK_DATABASE_NAME = "track_database.db"

val databaseModule = module {
    single {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN downloadSpeedMegaBitsPerSec TEXT")
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN uploadSpeedMegaBitsPerSec TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN cellBand TEXT")
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN cellBandName TEXT")
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN cellBandNameInformal TEXT")
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN cellBandFrequencyUpload TEXT")
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN cellBandFrequencyDownload TEXT")

            }
        }

        Room.databaseBuilder(
            androidApplication(),
            TrackDatabase::class.java,
            TRACK_DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()


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

    single {
        DatabaseManager(
            get(),
            get(),
            get(),
        )
    }

}