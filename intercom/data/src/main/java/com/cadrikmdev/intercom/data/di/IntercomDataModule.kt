package com.cadrikmdev.intercom.data.di

import com.cadrikmdev.intercom.data.AndroidMessageProcessor
import com.cadrikmdev.intercom.data.client.AndroidBluetoothClientService
import com.cadrikmdev.intercom.data.server.AndroidBluetoothServerService
import com.cadrikmdev.intercom.domain.client.BluetoothClientService
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import com.cadrikmdev.intercom.domain.server.BluetoothServerService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val intercomDataModule = module {
    singleOf(::AndroidBluetoothServerService).bind<BluetoothServerService>()
    singleOf(::AndroidBluetoothClientService).bind<BluetoothClientService>()
    singleOf(::AndroidMessageProcessor).bind<MessageProcessor>()
}