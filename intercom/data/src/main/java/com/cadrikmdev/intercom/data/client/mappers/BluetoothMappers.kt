package com.cadrikmdev.intercom.data.client.mappers

import android.bluetooth.BluetoothDevice
import com.cadrikmdev.intercom.domain.client.DeviceNode
import timber.log.Timber

fun BluetoothDevice.toDeviceNode(): DeviceNode? {
    return try {
        DeviceNode(
            address = this.address,
            displayName = this.name,
            isPaired = this.bondState == BluetoothDevice.BOND_BONDED,
            type = this.type
        )
    } catch (e: SecurityException) {
        e.printStackTrace()
        Timber.e(e.message)
        null
    }
}