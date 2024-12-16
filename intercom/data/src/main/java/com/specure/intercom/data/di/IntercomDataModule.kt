package com.specure.intercom.data.di

import android.bluetooth.BluetoothManager
import androidx.core.content.getSystemService
import com.specure.intercom.data.AndroidMessageProcessor
import com.specure.intercom.data.client.AndroidBluetoothBleClientService
import com.specure.intercom.data.client.AndroidBluetoothClientService
import com.specure.intercom.data.server.AndroidBluetoothAdvertiser
import com.specure.intercom.data.server.AndroidBluetoothBleServerService
import com.specure.intercom.data.server.AndroidBluetoothServerService
import com.specure.intercom.domain.client.BluetoothClientService
import com.specure.intercom.domain.message.MessageProcessor
import com.specure.intercom.domain.server.BluetoothAdvertiser
import com.specure.intercom.domain.server.BluetoothServerService
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


const val DI_BLUETOOTH_CLIENT_SERVICE_BLE = "bluetoothLowEnergyClientService"
const val DI_BLUETOOTH_CLIENT_SERVICE_CLASSIC = "bluetoothClientService"

const val DI_BLUETOOTH_SERVER_SERVICE_BLE = "bluetoothLowEnergyServerService"
const val DI_BLUETOOTH_SERVER_SERVICE_CLASSIC = "bluetoothServerService"

val intercomDataModule = module {
    single<BluetoothServerService>(named(DI_BLUETOOTH_SERVER_SERVICE_CLASSIC)) {
        AndroidBluetoothServerService(
            get(),
            get(),
            get()
        )
    }

    single<BluetoothServerService>(named(DI_BLUETOOTH_SERVER_SERVICE_BLE)) {
        AndroidBluetoothBleServerService(
            get(),
            get(),
            get()
        )
    }

    single<BluetoothClientService>(named(DI_BLUETOOTH_CLIENT_SERVICE_CLASSIC)) {
        AndroidBluetoothClientService(
            get(),
            get(),
            get(),
            get()
        )
    }

    single<BluetoothClientService>(named(DI_BLUETOOTH_CLIENT_SERVICE_BLE)) {
        AndroidBluetoothBleClientService(
            get(),
            get(),
            get()
        )
    }

    single { androidApplication().getSystemService<BluetoothManager>() }

    single { androidApplication().getSystemService<BluetoothManager>()?.adapter }

    singleOf(::AndroidMessageProcessor).bind<MessageProcessor>()
    singleOf(::AndroidBluetoothAdvertiser).bind<BluetoothAdvertiser>()
}