package com.specure.intercom.data.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.specure.intercom.data.util.isBluetoothAdvertisePermissionGranted
import com.specure.intercom.domain.ManagerControlServiceProtocol
import com.specure.intercom.domain.server.BluetoothAdvertiser
import timber.log.Timber
import java.util.UUID

class AndroidBluetoothAdvertiser(
    private val context: Context
) : BluetoothAdvertiser {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var advertiser: BluetoothLeAdvertiser? = null

    // UUID to broadcast
    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID

    override fun startAdvertising() {
        if (bluetoothAdapter?.isEnabled != true) {
            Timber.e("Bluetooth is not enabled.")
            return
        }

        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            Timber.e("Device does not support BLE advertising.")
            return
        }

        // Configure Advertise Settings
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        // Configure Advertise Data
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true) // Optionally include device name
            .addServiceUuid(ParcelUuid(serviceUUID)) // Add the service UUID
            .build()

        // Start Advertising
        if (!context.isBluetoothAdvertisePermissionGranted()) {
            return
        }
        advertiser?.startAdvertising(settings, data, advertiseCallback)
        Timber.d("Advertising started with UUID: $serviceUUID")
    }

    override fun stopAdvertising() {
        if (!context.isBluetoothAdvertisePermissionGranted()) {
            return
        }
        advertiser?.stopAdvertising(advertiseCallback)
        Timber.d("Advertising stopped.")
    }

    // Callback to handle advertising events
    private val advertiseCallback = object : android.bluetooth.le.AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Timber.d("Advertising started successfully.")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Timber.e("Advertising failed with error code: $errorCode")
        }
    }
}
