package com.cadrikmdev.intercom.data.client

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cadrikmdev.core.domain.util.Result
import com.cadrikmdev.intercom.data.client.mappers.toDeviceNode
import com.cadrikmdev.intercom.domain.ManagerControlServiceProtocol
import com.cadrikmdev.intercom.domain.client.BluetoothClientService
import com.cadrikmdev.intercom.domain.client.BluetoothError
import com.cadrikmdev.intercom.domain.client.DeviceNode
import com.cadrikmdev.intercom.domain.client.DeviceType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import java.io.IOException

class AndroidBluetoothClientService(
    private val context: Context,
    private val applicationScope: CoroutineScope,
) : BluetoothClientService {

    private var _pairedDevices = MutableStateFlow<Set<BluetoothDevice>>(setOf())
    val pairedDevices = _pairedDevices.asStateFlow()

    private var _connections = MutableStateFlow<Map<String, BluetoothSocket>>(mapOf())
    val connections = _connections.asStateFlow()

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun observeConnectedDevices(localDeviceType: DeviceType): Flow<Set<DeviceNode>> {
        return callbackFlow {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Timber.e("Device doesn't support Bluetooth")
                send(setOf())
                return@callbackFlow
            }

            if (bluetoothAdapter?.isEnabled != true) {
                // Bluetooth is not enabled
                Timber.d("Bluetooth is not enabled")
                // You can request user to enable Bluetooth here
                send(setOf())
                return@callbackFlow
            }

            bluetoothAdapter?.let {
                if (getPairedDevicesEndedWithError(it)) return@callbackFlow
            }

            // BroadcastReceiver for changes in bonded state
            val bondStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action

                    val pairedDevices = bluetoothAdapter?.let {
                        getPairedDevices(it)
                    }
                    Timber.d("Updating paired devices: $pairedDevices")

                    val pairedNodes: Set<DeviceNode> = pairedDevices?.mapNotNull {
                        it.toDeviceNode()
                    }?.toSet() ?: setOf()
                    trySend(pairedNodes)

                    if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    }
                }
            }

            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(bondStateReceiver, filter)

            awaitClose {
                context.unregisterReceiver(bondStateReceiver)
                Timber.d("Unregistered bluetooth change receiver")
            }
        }
    }

    private suspend fun ProducerScope<Set<DeviceNode>>.getPairedDevicesEndedWithError(
        bluetoothAdapter: BluetoothAdapter
    ): Boolean {
        try {
            val pairedDevices: Set<BluetoothDevice> = getPairedDevices(bluetoothAdapter)
            Timber.d("Obtaining paired devices ${pairedDevices}")
            val pairedNodes: Set<DeviceNode> =
                pairedDevices.mapNotNull { it.toDeviceNode() }.toSet()
            trySend(pairedNodes)
        } catch (e: SecurityException) {
            awaitClose()
            return true
        }
        return false
    }

    private fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): Set<BluetoothDevice> {
        val pairedDevices: Set<BluetoothDevice> = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            setOf()
        } else {
            bluetoothAdapter.bondedDevices
        }
        applicationScope.launch {
            _pairedDevices.emit(pairedDevices)
        }
        return pairedDevices
    }

    override suspend fun connectToDevice(deviceAddress: String): Result<Boolean, BluetoothError> {
        // Ensure the location permission is granted (required for Bluetooth discovery from Android M+)
        if (isFineLocationPermissionGranted()) return Result.Error(BluetoothError.NO_FINE_LOCATION_PERMISSIONS)
        if (!isBluetoothConnectPermissionGranted()) {
            return Result.Error(BluetoothError.MISSING_BLUETOOTH_CONNECT_PERMISSION)
        }

        val bluetoothDevice = getBluetoothDeviceFromDeviceAddress(deviceAddress)
            ?: return Result.Error(BluetoothError.BLUETOOTH_DEVICE_NOT_FOUND)

        // Use CompletableDeferred to wait for the result
        val resultDeferred = CompletableDeferred<Result<Boolean, BluetoothError>>()

        Timber.d("Connecting to device with address: ${deviceAddress}")
        Timber.d("Service uuid: ${ManagerControlServiceProtocol.customServiceUUID}")
        Timber.d("Connecting to device with supportedServices; ${bluetoothDevice.uuids.forEach { "${it.uuid}, " }}")
        bluetoothDevice.createRfcommSocketToServiceRecord(ManagerControlServiceProtocol.customServiceUUID)
        bluetoothDevice?.let { device ->
            val clientSocket: BluetoothSocket =
                device.createRfcommSocketToServiceRecord(
                    ManagerControlServiceProtocol.customServiceUUID
                )
            // Accept connections from clients (running in a separate thread)
            Thread {
                clientSocket?.let { socket ->
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    try {
                        socket.connect()
                        Timber.d("Connected to server socket successfully on ${bluetoothDevice.address}")
                    } catch (e: IOException) {
                        Timber.e("Unable to connect to server socket ${e.printStackTrace()}")
                    }

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    manageConnectedSocket(socket)
                }

            }.start()
        }
        return Result.Success(true)
    }

    override fun disconnectFromDevice(deviceAddress: String): Boolean {
        try {
            if (_connections.value[deviceAddress] == null) {
                return true
            }
            _connections.value[deviceAddress]?.close()
            _connections.value = removeKeyFromMap(deviceAddress)
            return true
        } catch (e: IOException) {
            Timber.e(e, "Error occurred while closing the socket")
            return false
        }

    }

    private fun removeKeyFromMap(deviceAddress: String) =
        _connections.value.filter { it.key != deviceAddress }

    private fun manageConnectedSocket(socket: BluetoothSocket) {
        // Implement logic for communication with the connected server
        _connections.value = _connections.value.plus(Pair(socket.remoteDevice.address, socket))
        // Launch a coroutine for receiving data

        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = socket.inputStream
            val outputStream = socket.outputStream
            val reader = inputStream.bufferedReader()

            try {
                // Using supervisorScope to ensure child coroutines are handled properly
                supervisorScope {
                    // Coroutine for receiving data
                    val receiveJob = launch {
                        try {
                            while (isActive) { // Check if the coroutine is still active
                                val message = reader.readLine() ?: break
                                Timber.d("Received: $message")
                                // Handle the received message
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during receiving data")
                        }
                    }

                    // Coroutine for sending data
                    val sendJob = launch {
                        try {
                            while (isActive) { // Check if the coroutine is still active
                                val message = "Client message"
                                outputStream.write((message + "\n").toByteArray())
                                outputStream.flush()
                                delay(5000) // Wait for 5 seconds before sending the next message
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during sending data")
                        }
                    }

                    // Wait for both jobs to complete, or for cancellation
                    receiveJob.join()
                    sendJob.join()
                }
            } catch (e: IOException) {
                Timber.e(e, "Error occurred during communication")
            } finally {
                // Ensure the streams and socket are closed
                try {
                    reader.close()
                    outputStream.close()
                    socket.close()
                    Timber.d("Socket closed: ${socket.remoteDevice.address}")
                } catch (e: IOException) {
                    Timber.e(e, "Error occurred while closing the socket")
                }
            }
        }
    }

    private fun isFineLocationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
    }


    private fun isBluetoothConnectPermissionGranted() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private fun getBluetoothDeviceFromDeviceAddress(deviceAddress: String): BluetoothDevice? {
        return try {
            _pairedDevices.value.first { it.address == deviceAddress }
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            null
        }
    }
}