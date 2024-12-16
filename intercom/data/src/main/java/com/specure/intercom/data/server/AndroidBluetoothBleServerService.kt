package com.specure.intercom.data.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.specure.core.domain.package_info.PackageInfoProvider
import com.specure.intercom.data.util.isBluetoothConnectPermissionGranted
import com.specure.intercom.domain.ManagerControlServiceProtocol
import com.specure.intercom.domain.data.MeasurementProgress
import com.specure.intercom.domain.data.MeasurementState
import com.specure.intercom.domain.message.MessageProcessor
import com.specure.intercom.domain.message.TrackerAction
import com.specure.intercom.domain.server.BluetoothServerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

class AndroidBluetoothBleServerService(
    private val context: Context,
    private val messageProcessor: MessageProcessor,
    private val packageInfoProvider: PackageInfoProvider
) : BluetoothServerService {

    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID
    private val characteristicUUID: UUID =
        UUID.randomUUID() // Replace with your own characteristic UUID

    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    private var gattServer: BluetoothGattServer? = null

    private val connectedDevices = mutableSetOf<String>()

    private val _receivedActionFlow = MutableSharedFlow<TrackerAction?>()
    override val receivedActionFlow: SharedFlow<TrackerAction?> get() = _receivedActionFlow

    var getStatusUpdate: () -> MeasurementProgress? = {
        MeasurementProgress(
            state = MeasurementState.NOT_ACTIVATED,
            errors = null,
            appVersion = null,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?) {
        this.getStatusUpdate = statusUpdate
    }

    override fun startServer() {
        if (!context.isBluetoothConnectPermissionGranted() || bluetoothAdapter?.isEnabled != true) {
            Timber.e("Bluetooth not enabled or permissions not granted")
            return
        }

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)?.apply {
            addService(createGattService())
        }

        Timber.d("BLE GATT server started with service UUID: $serviceUUID")
    }

    private fun createGattService(): BluetoothGattService {
        // Create a GATT service with a single characteristic
        val service = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val characteristic = android.bluetooth.BluetoothGattCharacteristic(
            characteristicUUID,
            android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ or
                    android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE or
                    android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ or
                    android.bluetooth.BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(characteristic)
        return service
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: android.bluetooth.BluetoothDevice,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Timber.d("Device connected: ${device.address}")
                connectedDevices.add(device.address)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.d("Device disconnected: ${device.address}")
                connectedDevices.remove(device.address)
            }
        }

        override fun onCharacteristicReadRequest(
            device: android.bluetooth.BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: android.bluetooth.BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == characteristicUUID) {
                val progress = getStatusUpdate()?.state?.name?.toByteArray() ?: byteArrayOf()
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    progress
                )
                Timber.d("Read request from ${device.address}: ${String(progress)}")
            }
        }

        override fun onCharacteristicWriteRequest(
            device: android.bluetooth.BluetoothDevice,
            requestId: Int,
            characteristic: android.bluetooth.BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (characteristic.uuid == characteristicUUID) {
                val message = String(value)
                Timber.d("Write request from ${device.address}: $message")

                CoroutineScope(Dispatchers.IO).launch {
                    val action = messageProcessor.processMessage(message)
                    _receivedActionFlow.emit(action)
                }

                if (responseNeeded) {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        null
                    )
                }
            }
        }

        override fun onNotificationSent(device: android.bluetooth.BluetoothDevice?, status: Int) {
            Timber.d("Notification sent to ${device?.address}: Status $status")
        }
    }


    override fun stopServer() {
        if (context.isBluetoothConnectPermissionGranted()) {
            gattServer?.close()
        }
        gattServer = null
        connectedDevices.clear()
        Timber.d("BLE GATT server stopped")
    }

}
