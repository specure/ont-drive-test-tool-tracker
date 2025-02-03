package com.specure.signaltracker.di

import AndroidBluetoothService
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.core.content.getSystemService
import com.cadrikmdev.intercom.data.AndroidBluetoothDevicesProvider
import com.cadrikmdev.intercom.data.AndroidMessageProcessor
import com.cadrikmdev.intercom.data.client.AndroidBluetoothBleClientService
import com.cadrikmdev.intercom.data.client.AndroidBluetoothClientService
import com.cadrikmdev.intercom.data.server.AndroidBluetoothAdvertiser
import com.cadrikmdev.intercom.data.server.AndroidBluetoothBleServerService
import com.cadrikmdev.intercom.data.server.AndroidBluetoothServerService
import com.cadrikmdev.intercom.domain.BluetoothDevicesProvider
import com.cadrikmdev.intercom.domain.client.BluetoothClientService
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import com.cadrikmdev.intercom.domain.message.SerializableContent
import com.cadrikmdev.intercom.domain.server.BluetoothAdvertiser
import com.cadrikmdev.intercom.domain.server.BluetoothServerService
import com.cadrikmdev.intercom.domain.service.BluetoothService
import com.specure.signaltracker.intercom.AppBluetoothServiceSpecification
import com.specure.track.domain.intercom.data.MeasurementProgressContent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module


const val DI_BLUETOOTH_CLIENT_SERVICE_BLE = "bluetoothLowEnergyClientService"
const val DI_BLUETOOTH_CLIENT_SERVICE_CLASSIC = "bluetoothClientService"

const val DI_BLUETOOTH_SERVER_SERVICE_BLE = "bluetoothLowEnergyServerService"
const val DI_BLUETOOTH_SERVER_SERVICE_CLASSIC = "bluetoothServerService"

val appIntercomModule = module {

    single<BluetoothServerService>(named(DI_BLUETOOTH_SERVER_SERVICE_CLASSIC)) {
        AndroidBluetoothServerService(
            get(),
            get(),
            get(),
        )
    }

    single<BluetoothServerService>(named(DI_BLUETOOTH_SERVER_SERVICE_BLE)) {
        AndroidBluetoothBleServerService(
            get(),
            get(),
            get(),
        )
    }

    single<BluetoothClientService>(named(DI_BLUETOOTH_CLIENT_SERVICE_CLASSIC)) {
        AndroidBluetoothClientService(
            get(),
            get(),
            get(),
            get(),
            get(),
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

    single<MessageProcessor> {
        AndroidMessageProcessor(
            otherSerializers = SerializersModule {
                polymorphic(SerializableContent::class) {
                    subclass(
                        MeasurementProgressContent::class,
                        MeasurementProgressContent.serializer()
                    )
                }
            }
        )
    }
    singleOf(::AndroidBluetoothAdvertiser).bind<BluetoothAdvertiser>()

    singleOf(::AndroidBluetoothDevicesProvider).bind<BluetoothDevicesProvider<BluetoothDevice>>()
    singleOf(::AndroidBluetoothService).bind<BluetoothService>()
    singleOf(::AppBluetoothServiceSpecification).bind<com.cadrikmdev.intercom.domain.BluetoothServiceSpecification>()
}