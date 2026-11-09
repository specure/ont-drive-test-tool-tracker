package com.specure.track.data.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.specure.track.data.ping.PingRunnerIcmp4a
import com.specure.track.domain.ping.PingRunner
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

import org.koin.dsl.module

val trackDataModule = module {
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(androidApplication())
    }
    singleOf(::PingRunnerIcmp4a).bind<PingRunner>()
}