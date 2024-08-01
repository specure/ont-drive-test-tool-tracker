package com.cadrikmdev.track.data

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.cadrikmdev.track.domain.BluetoothService
import com.cadrikmdev.track.domain.ManagerControlServiceProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import java.io.IOException
import java.util.UUID

class AndroidBluetoothService(private val context: Context) : BluetoothService {

    private val serviceUUID: UUID = ManagerControlServiceProtocol.customServiceUUID

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

        Timber.d("starting listenning for connections with service uuid = ${serviceUUID}")
        // You can set up Bluetooth classic (RFCOMM) or use BLE advertising for discovery
        // For RFCOMM, you would use something like the following:
        bluetoothAdapter?.let {
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
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during receiving data")
                        } catch (e: Exception) {
                            Timber.e(e, "Unexpected error in receiving coroutine: ${e.message}")
                        }
                    }

                    // Coroutine for sending data
                    val sendJob = launch {
                        try {
                            while (isActive) {
                                val message = "Server message"
                                outputStream.write((message + "\n").toByteArray())
                                outputStream.flush()
                                delay(5000) // Wait for 5 seconds before sending the next message
                            }
                        } catch (e: IOException) {
                            Timber.e(e, "Error occurred during sending data")
                        } catch (e: Exception) {
                            Timber.e(e, "Unexpected error in sending coroutine: ${e.message}")
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