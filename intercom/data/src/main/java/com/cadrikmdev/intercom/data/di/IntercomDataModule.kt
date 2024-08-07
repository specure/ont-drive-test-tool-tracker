package com.cadrikmdev.intercom.data.di

import com.cadrikmdev.intercom.data.AndroidBluetoothServerService
import com.cadrikmdev.intercom.data.AndroidMessageProcessor
import com.cadrikmdev.intercom.domain.BluetoothServerService
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

import org.koin.dsl.module

val intercomDataModule = module {
    singleOf(::AndroidBluetoothServerService).bind<BluetoothServerService>()
    singleOf(::AndroidMessageProcessor).bind<MessageProcessor>()
}