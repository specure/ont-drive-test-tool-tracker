package com.specure.connectivity.presentation.mobile_network.di

import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.specure.connectivity.domain.NetworkTracker
import com.specure.connectivity.presentation.mobile_network.AndroidNetworkTracker
import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mobileNetworkModule = module {
    single { NetMonsterFactory.get(androidApplication()) }.bind<INetMonster>()
    single { androidApplication().getSystemService<TelephonyManager>() }
    single { androidApplication().getSystemService<SubscriptionManager>() }
    single { androidApplication().getSystemService<ConnectivityManager>() }
    single { androidApplication().getSystemService<WifiManager>() }
    singleOf(::AndroidNetworkTracker).bind<NetworkTracker>()
}