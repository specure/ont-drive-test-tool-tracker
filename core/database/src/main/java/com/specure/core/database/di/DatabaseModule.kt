package com.specure.core.database.di

import android.app.NotificationManager
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.specure.core.database.RoomLocalTrackDataSource
import com.specure.core.database.Tables
import com.specure.core.database.TrackDatabase
import com.specure.core.database.export.CoreFileProvider
import com.specure.core.database.export.DatabaseManager
import com.specure.core.database.export.DatabaseWorkerScheduler
import com.specure.core.database.export.DeleteAllWorker
import com.specure.core.database.export.DeleteExportedWorker
import com.specure.core.database.export.MarkAsExportedWorker
import com.specure.core.database.export.TracksExporter
import com.specure.core.database.export.WorkerScheduler
import com.specure.core.domain.track.LocalTrackDataSource
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // creating temp table without cellBandFrequencyUpload field
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `track_entity_new` " +
                            "(`durationMillis` INTEGER NOT NULL, " +
                            "`timestamp` TEXT NOT NULL, " +
                            "`timestampRaw` INTEGER NOT NULL, " +
                            "`downloadSpeed` REAL, " +
                            "`downloadSpeedUnit` TEXT, " +
                            "`downloadSpeedMegaBitsPerSec` TEXT, " +
                            "`downloadSpeedTestState` TEXT, " +
                            "`downloadSpeedTestError` TEXT, " +
                            "`downloadSpeedTestTimestamp` TEXT, " +
                            "`downloadSpeedTestTimestampRaw` INTEGER, " +
                            "`uploadSpeed` REAL, " +
                            "`uploadSpeedUnit` TEXT, " +
                            "`uploadSpeedMegaBitsPerSec` TEXT, " +
                            "`uploadSpeedTestState` TEXT, " +
                            "`uploadSpeedTestError` TEXT, " +
                            "`uploadSpeedTestTimestamp` TEXT, " +
                            "`uploadSpeedTestTimestampRaw` INTEGER, " +
                            "`latitude` REAL, `longitude` REAL, " +
                            "`locationTimestamp` TEXT, " +
                            "`locationTimestampRaw` INTEGER, " +
                            "`networkType` TEXT, " +
                            "`mobileNetworkOperator` TEXT, " +
                            "`mobileNetworkType` TEXT, " +
                            "`signalStrength` INTEGER, " +
                            "`networkInfoTimestamp` TEXT, " +
                            "`networkInfoTimestampRaw` INTEGER, " +
                            "`connectionStatus` TEXT, " +
                            "`temperatureCelsius` REAL, " +
                            "`temperatureTimestamp` TEXT, " +
                            "`temperatureTimestampRaw` INTEGER, " +
                            "`cellBand` TEXT, " +
                            "`cellBandFrequencyDownload` TEXT, " +
                            "`cellBandName` TEXT, " +
                            "`cellBandNameInformal` TEXT, " +
                            "`exported` INTEGER NOT NULL, " +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT)"
                                .trimIndent()
                )
                db.execSQL(
                    """
                        INSERT INTO track_entity_new (
                           id,
                           durationMillis,
                           timestamp,
                           timestampRaw,
                           downloadSpeed,
                           downloadSpeedUnit,
                           downloadSpeedMegaBitsPerSec,
                           downloadSpeedTestState,
                           downloadSpeedTestError,
                           downloadSpeedTestTimestamp,
                           downloadSpeedTestTimestampRaw,
                           uploadSpeed, 
                           uploadSpeedUnit, 
                           uploadSpeedMegaBitsPerSec, 
                           uploadSpeedTestState,
                           uploadSpeedTestError,
                           uploadSpeedTestTimestamp,
                           uploadSpeedTestTimestampRaw,
                           latitude,
                           locationTimestamp,
                           locationTimestampRaw,
                           networkType,     
                           mobileNetworkOperator,
                           mobileNetworkType, 
                           signalStrength, 
                           networkInfoTimestamp,
                           networkInfoTimestampRaw,
                           connectionStatus,
                           temperatureCelsius,
                           temperatureTimestamp,
                           temperatureTimestampRaw,
                           cellBand,    
                           cellBandFrequencyDownload, 
                           cellBandName, 
                           cellBandNameInformal,
                           exported
                           )
                        SELECT 
                            id,
                           durationMillis,
                           timestamp,
                           timestampRaw,
                           downloadSpeed,
                           downloadSpeedUnit,
                           downloadSpeedMegaBitsPerSec,
                           downloadSpeedTestState,
                           downloadSpeedTestError,
                           downloadSpeedTestTimestamp,
                           downloadSpeedTestTimestampRaw,
                           uploadSpeed, 
                           uploadSpeedUnit, 
                           uploadSpeedMegaBitsPerSec, 
                           uploadSpeedTestState,
                           uploadSpeedTestError,
                           uploadSpeedTestTimestamp,
                           uploadSpeedTestTimestampRaw,
                           latitude,
                           locationTimestamp,
                           locationTimestampRaw,
                           networkType,     
                           mobileNetworkOperator,
                           mobileNetworkType, 
                           signalStrength, 
                           networkInfoTimestamp,
                           networkInfoTimestampRaw,
                           connectionStatus,
                           temperatureCelsius,
                           temperatureTimestamp,
                           temperatureTimestampRaw,
                           cellBand,    
                           cellBandFrequencyDownload, 
                           cellBandName, 
                           cellBandNameInformal,
                           exported
                        FROM track_entity
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE track_entity")
                db.execSQL("ALTER TABLE track_entity_new RENAME TO track_entity")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ${Tables.TRACK_ENTITY} ADD COLUMN appVersion TEXT")
            }
        }


        Room.databaseBuilder(
            androidApplication(),
            TrackDatabase::class.java,
            TRACK_DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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