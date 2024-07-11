package com.cadrikmdev.track.data.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.koin.android.ext.koin.androidApplication

import org.koin.dsl.module

val trackDataModule = module {
    single<SharedPreferences> {
        PreferenceManager.getDefaultSharedPreferences(androidApplication())
    }
}