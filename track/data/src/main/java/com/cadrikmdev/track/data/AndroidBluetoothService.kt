package com.cadrikmdev.track.data

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.cadrikmdev.track.domain.BluetoothService
import com.cadrikmdev.track.domain.ManagerControlServiceProtocol
import java.util.UUID

class AndroidBluetoothService(private val context: Context) : BluetoothService {

    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID
    private val characteristicUUID: UUID =
        UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") // Replace with your unique UUID

    private var bluetoothServerSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null

    override fun startGattServer() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Ensure Bluetooth is supported and enabled on the device
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            // Bluetooth is not supported or not enabled
            return
        }

        // Create the GATT service
        val gattService =
            BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Create the GATT characteristic
        val characteristic = BluetoothGattCharacteristic(
            characteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        // Add the characteristic to the service
        gattService.addCharacteristic(characteristic)

        // TODO: Add your service to a Bluetooth GATT server here (Not available in Android SDK directly, usually part of peripheral devices)

        // You can set up Bluetooth classic (RFCOMM) or use BLE advertising for discovery
        // For RFCOMM, you would use something like the following:
        val serverSocket: BluetoothServerSocket? =
            bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyService", serviceUUID)
        // Accept connections from clients (running in a separate thread)
        Thread {
            while (true) {
                val socket: BluetoothSocket? = serverSocket?.accept()
                socket?.let {
                    // Handle the connection in a separate thread
                    manageConnectedSocket(it)
                }
            }
        }.start()
    }

    private fun manageConnectedSocket(socket: BluetoothSocket) {
        // Implement logic for communication with the connected client
        bluetoothSocket = socket
        // Read/write data using socket.inputStream and socket.outputStream
    }

    override fun stopGattServer() {
        bluetoothServerSocket?.close()
        bluetoothSocket?.close()
    }
}