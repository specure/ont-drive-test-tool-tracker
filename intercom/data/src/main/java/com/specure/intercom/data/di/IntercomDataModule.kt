package com.specure.intercom.data.di

import com.specure.intercom.data.AndroidMessageProcessor
import com.specure.intercom.data.client.AndroidBluetoothClientService
import com.specure.intercom.data.server.AndroidBluetoothServerService
import com.specure.intercom.domain.client.BluetoothClientService
import com.specure.intercom.domain.message.MessageProcessor
import com.specure.intercom.domain.server.BluetoothServerService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val intercomDataModule = module {
    singleOf(::AndroidBluetoothServerService).bind<BluetoothServerService>()
    singleOf(::AndroidBluetoothClientService).bind<BluetoothClientService>()
    singleOf(::AndroidMessageProcessor).bind<MessageProcessor>()
}