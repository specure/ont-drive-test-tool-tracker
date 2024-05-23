package com.cadrikmdev.core.connectivty.mobile_network.di

import cz.mroczis.netmonster.core.INetMonster
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val mobileNetworkModule = module {
    single { NetMonsterFactory.get(androidApplication()) }.bind<INetMonster>()
}