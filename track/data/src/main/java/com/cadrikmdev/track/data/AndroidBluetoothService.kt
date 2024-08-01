package com.cadrikmdev.track.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import com.cadrikmdev.track.domain.BluetoothService
import com.cadrikmdev.track.domain.ManagerControlServiceProtocol
import timber.log.Timber
import java.io.IOException
import java.util.UUID

class AndroidBluetoothService(private val context: Context) : BluetoothService {

    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID
    private val characteristicUUID: UUID =
        ManagerControlServiceProtocol.customCharacteristicServiceUUID


    private var bluetoothServerSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var gattServer: BluetoothGattServer? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun startGattServer() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Ensure Bluetooth is supported and enabled on the device
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled != true) {
            // Bluetooth is not supported or not enabled
            return
        }
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        // Create the GATT service
        val gattService =
            BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Create the GATT characteristic
        val characteristic = BluetoothGattCharacteristic(
            characteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // Optionally add a descriptor
        val descriptor = BluetoothGattDescriptor(
            characteristicUUID, // Client Characteristic Configuration UUID
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        characteristic.addDescriptor(descriptor)

        // Add the characteristic to the service
        gattService.addCharacteristic(characteristic)

        // TODO: Add your service to a Bluetooth GATT server here (Not available in Android SDK directly, usually part of peripheral devices)


        gattServer?.addService(gattService)
        bluetoothAdapter?.let {
            startAdvertising(it)
        }

        Timber.d("starting listenning for connections with service uuid = ${serviceUUID}")
        // You can set up Bluetooth classic (RFCOMM) or use BLE advertising for discovery
        // For RFCOMM, you would use something like the following:
        bluetoothAdapter?.let {
            val serverSocket: BluetoothServerSocket? =
                it.listenUsingRfcommWithServiceRecord("MyService", serviceUUID)
            // Accept connections from clients (running in a separate thread)
            Thread {
                var shouldLoop = true
                while (shouldLoop) {
                    try {
                        val socket: BluetoothSocket? = serverSocket?.accept()
                        socket?.let {
                            // Handle the connection in a separate thread
                            manageConnectedSocket(it)
                        }
                    } catch (e: IOException) {
                        Timber.e("Socket's accept() method failed", e)
                        shouldLoop = false
                    }
                }
            }.start()
        }
    }

    private fun startAdvertising(bluetoothAdapter: BluetoothAdapter) {
        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Device connected
                Timber.d("GATT connected successfully")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Device disconnected
                Timber.d("GATT disconnected")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int,
            offset: Int, characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if (characteristic.uuid == characteristicUUID) {
                val value = byteArrayOf(0x01, 0x02) // Example value
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int,
            characteristic: BluetoothGattCharacteristic, preparedWrite: Boolean,
            responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic.uuid == characteristicUUID) {
                // Handle the write request
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int,
            descriptor: BluetoothGattDescriptor, preparedWrite: Boolean,
            responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (descriptor.uuid == characteristicUUID) {
                // Handle descriptor write request
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Timber.d("Advertising started successfully")
            // Advertising started successfully
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Advertising failed
            Timber.d("Advertising started failed")
        }
    }
    private fun manageConnectedSocket(socket: BluetoothSocket) {
        // Implement logic for communication with the connected client
        bluetoothSocket = socket
        // Read/write data using socket.inputStream and socket.outputStream
    }

    fun onDestroy() {
        gattServer?.close()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    override fun stopGattServer() {
        bluetoothServerSocket?.close()
        bluetoothSocket?.close()
    }
}