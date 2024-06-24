package com.cadrikmdev.connectivity.presentation.network.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.cadrikmdev.connectivity.domain.ConnectivityObserver
import com.cadrikmdev.connectivity.presentation.network.NetworkConnectivityObserver
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val connectivityModule = module {
    singleOf(::NetworkConnectivityObserver).bind<ConnectivityObserver>()
    single { androidApplication().getSystemService<ConnectivityManager>() }
}