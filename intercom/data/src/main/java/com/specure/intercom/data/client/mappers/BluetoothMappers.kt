package com.specure.intercom.data.client.mappers

import android.bluetooth.BluetoothDevice
import com.specure.intercom.domain.client.DeviceNode
import com.specure.intercom.domain.client.TrackingDevice
import com.specure.intercom.domain.data.MeasurementState
import timber.log.Timber

fun BluetoothDevice.toDeviceNode(): DeviceNode? {
    return try {
        DeviceNode(
            address = this.address,
            displayName = this.name,
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Timber.e(e.message)
        null
    }
}

fun BluetoothDevice.toTrackingDevice(): TrackingDevice? {
    return try {
        TrackingDevice(
            name = this.name,
            address = this.address,
            connected = false,
            status = MeasurementState.UNKNOWN,
            deviceAppVersion = "",
            updateTimestamp = System.currentTimeMillis()
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Timber.e(e.message)
        null
    }
}

fun BluetoothDevice.toBluetoothDevice(): com.specure.intercom.domain.data.BluetoothDevice? {
    return try {
        com.specure.intercom.domain.data.BluetoothDevice(
            name = this.name,
            address = this.address,
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Timber.e(e.message)
        null
    }
}

fun com.specure.intercom.domain.data.BluetoothDevice.toTrackingDevice(): TrackingDevice? {
    return try {
        TrackingDevice(
            name = this.name,
            address = this.address,
            connected = false,
            status = MeasurementState.UNKNOWN,
            deviceAppVersion = "",
            updateTimestamp = System.currentTimeMillis()
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Timber.e(e.message)
        null
    }
}