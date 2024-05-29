package com.cadrikmdev.core.connectivty.presentation.network.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.cadrikmdev.core.connectivty.presentation.network.NetworkConnectivityObserver
import com.cadrikmdev.core.connectivty.domain.connectivity.ConnectivityObserver
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val connectivityModule = module {
    singleOf(::NetworkConnectivityObserver).bind<com.cadrikmdev.core.connectivty.domain.connectivity.ConnectivityObserver>()
    single { androidApplication().getSystemService<ConnectivityManager>() }
}