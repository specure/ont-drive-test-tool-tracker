package com.cadrikmdev.track.data.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.cadrikmdev.track.data.AppConfig
import com.cadrikmdev.track.domain.config.Config
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val trackDataModule = module {
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(androidApplication())
    }

    singleOf(::AppConfig).bind<Config>()
}