package com.specure.signaltracker.di

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.specure.signaltracker.MainViewModel
import com.specure.signaltracker.SignalTrackerApp
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)

    single<SharedPreferences> {
        EncryptedSharedPreferences(
            androidApplication(),
            "auth_pref",
            MasterKey(androidApplication()),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    single<CoroutineScope> {
        (androidApplication() as SignalTrackerApp).applicationScope
    }
}