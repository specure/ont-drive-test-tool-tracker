package com.cadrikmdev.intercom.data.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.cadrikmdev.intercom.domain.ManagerControlServiceProtocol
import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import com.cadrikmdev.intercom.domain.data.MeasurementState
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import com.cadrikmdev.intercom.domain.message.TrackerAction
import com.cadrikmdev.intercom.domain.server.BluetoothServerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import java.io.IOException
import java.util.UUID

class AndroidBluetoothServerService(
    private val context: Context,
    private val messageProcessor: MessageProcessor,
) : BluetoothServerService {

    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID

    private var bluetoothServerSocket: BluetoothServerSocket? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var gattServer: BluetoothGattServer? = null
    private var bluetoothAdapter: BluetoothAdapter? = null

    var getStatusUpdate: () -> MeasurementProgress? = {
        MeasurementProgress(
            state = MeasurementState.NOT_ACTIVATED,
            error = null,
            timestamp = System.currentTimeMillis()
        )
    }

    private val _receivedActionFlow = MutableSharedFlow<TrackerAction?>()
    override val receivedActionFlow: SharedFlow<TrackerAction?> get() = _receivedActionFlow

    override fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?) {
        this.getStatusUpdate = statusUpdate
    }

    override fun startGattServer() {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Ensure Bluetooth is supported and enabled on the device
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled != true) {
            // Bluetooth is not supported or not enabled
            return
        }

        Timber.d("starting listenning for connections with service uuid = ${serviceUUID}")
        // You can set up Bluetooth classic (RFCOMM) or use BLE advertising for discovery
        // For RFCOMM, you would use something like the following:
        bluetoothAdapter?.let {
            if (bluetoothSocket?.isConnected == true) {
                Timber.d("Bluetooth socket is already created.")
                return
            }
            val serverSocket: BluetoothServerSocket? =
                it.listenUsingRfcommWithServiceRecord("MyService", serviceUUID)
            // Accept connections from clients (running in a separate thread)
            Timber.d("starting listenning for connections with service uuid = ${serviceUUID}")
            Thread {
                var shouldLoop = true
                Timber.d("Waiting for client to connect")
                while (shouldLoop) {
                    try {
                        Timber.d("Trying to establish conection with client")
                        val socket: BluetoothSocket? = serverSocket?.accept()
                        socket?.let {
                            // Handle the connection in a separate thread
                            Timber.d("Connection made successfully with client")
                            manageConnectedSocket(it)
                        }
                    } catch (e: IOException) {
                        Timber.e("Socket's accept() method failed", e)
                        shouldLoop = true // todo change to false
                    }
                }
            }.start()
        }
    }

    private fun manageConnectedSocket(socket: BluetoothSocket) {
        // Implement logic for communication with the connected client
        bluetoothSocket = socket

        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = socket.inputStream
            val outputStream = socket.outputStream
            val reader = inputStream.bufferedReader()

            try {
                // Using supervisorScope to manage child coroutines
                supervisorScope {
                    // Coroutine for receiving data
                    val receiveJob = launch {
                        try {
                            while (isActive) {
                                val message = reader.readLine() ?: break
                                Timber.d("Received: $message")
                                // Handle the received message
                                val action = messageProcessor.processMessage(message)
                                launch(Dispatchers.IO) {
                                    _receivedActionFlow.emit(action)
                                }
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during receiving data")
                        }
                    }

                    // Coroutine for sending data
                    val sendJob = launch {
                        try {
                            while (isActive) {
                                val message = getStatusUpdate()
                                message?.let {
                                    val encodedMessage = messageProcessor.sendAction(
                                        TrackerAction.UpdateProgress(progress = it)
                                    )
                                    Timber.d("Sending: $encodedMessage")
                                    val byteArray = encodedMessage?.toByteArray()
                                    byteArray?.let {
                                        outputStream.write(it)
                                        outputStream.flush()
                                    }
                                }
                                delay(1000) // Wait for 1 seconds before sending the next message
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during sending data")
                        }
                    }

                    // Await completion of both coroutines
                    receiveJob.join()
                    sendJob.join()
                }
            } catch (e: IOException) {
                Timber.e(e, "Error occurred during communication")
            } finally {
                // Ensure the streams and socket are closed properly
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

    override fun stopGattServer() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Timber.e(e, "Error occurred while closing the socket")
        }
    }
}