package com.cadrikmdev.core.connectivty.presentation.mobile_network.di

import android.net.ConnectivityManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.getSystemService
import com.cadrikmdev.core.connectivty.domain.connectivity.NetworkTracker
import com.cadrikmdev.core.connectivty.presentation.mobile_network.NetmonsterNetworkTracker
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
    singleOf(::NetmonsterNetworkTracker).bind<NetworkTracker>()
}