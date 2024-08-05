package com.cadrikmdev.intercom.data.di

import com.cadrikmdev.intercom.data.AndroidBluetoothServerService
import com.cadrikmdev.intercom.domain.BluetoothServerService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

import org.koin.dsl.module

val intercomDataModule = module {
    singleOf(::AndroidBluetoothServerService).bind<BluetoothServerService>()
}