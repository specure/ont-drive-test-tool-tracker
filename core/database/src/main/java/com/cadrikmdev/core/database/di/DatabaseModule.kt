package com.cadrikmdev.core.database.di

import androidx.room.Room
import com.cadrikmdev.core.database.RoomLocalTrackDataSource
import com.cadrikmdev.core.database.TrackDatabase
import com.cadrikmdev.core.domain.track.LocalTrackDataSource
import org.koin.android.ext.koin.androidApplication
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

    single { get<TrackDatabase>().trackDao }

    singleOf(::RoomLocalTrackDataSource).bind<LocalTrackDataSource>()
}