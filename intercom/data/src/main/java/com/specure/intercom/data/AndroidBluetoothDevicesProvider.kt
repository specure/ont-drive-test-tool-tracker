package com.specure.intercom.data

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.specure.intercom.data.client.mappers.toBluetoothDevice
import com.specure.intercom.data.util.isBluetoothConnectPermissionGranted
import com.specure.intercom.domain.BluetoothDevicesProvider
import com.specure.intercom.domain.client.DeviceType
import com.specure.intercom.domain.data.BluetoothDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AndroidBluetoothDevicesProvider(
    private val bluetoothAdapter: BluetoothAdapter,
    private val coroutineScope: CoroutineScope,
    val context: Context,
) : BluetoothDevicesProvider<android.bluetooth.BluetoothDevice> {

    private var _nativePairedDevices =
        MutableStateFlow<Map<String, android.bluetooth.BluetoothDevice>>(emptyMap())
    override val nativePairedDevices = _nativePairedDevices.asStateFlow()

    private var _nativeNearbyUnpairedDevices =
        MutableStateFlow<Map<String, android.bluetooth.BluetoothDevice>>(emptyMap())
    override val nativeNearbyUnpairedDevices = _nativeNearbyUnpairedDevices.asStateFlow()

    private var _pairedDevices = MutableStateFlow<Map<String, BluetoothDevice>>(emptyMap())
    override val pairedDevices = _pairedDevices.asStateFlow()

    private var _nearbyUnpairedDevices = MutableStateFlow<Map<String, BluetoothDevice>>(emptyMap())
    override val nearbyUnpairedDevices = _nearbyUnpairedDevices.asStateFlow()

    override fun getPairedDevices(): Map<String, BluetoothDevice> {
        val pairedBluetoothDevices = if (context.isBluetoothConnectPermissionGranted()) {
            getAndroidPairedDevices()
                .mapNotNull { nativeDevice ->
                    nativeDevice.toBluetoothDevice()
                }
                .associateBy { device ->
                    device.address
                }
        } else {
            mapOf()
        }

        coroutineScope.launch {
            _pairedDevices.emit(pairedBluetoothDevices)
        }

        return pairedBluetoothDevices
    }

    private fun getAndroidPairedDevices(): Set<android.bluetooth.BluetoothDevice> {
        val pairedDevices: Set<android.bluetooth.BluetoothDevice> =
            if (context.isBluetoothConnectPermissionGranted()) {
                bluetoothAdapter.bondedDevices
            } else {
                setOf()
            }
        coroutineScope.launch {
            val nativeDevices = pairedDevices.associateBy { it.address }.toMap()
            _nativePairedDevices.emit(nativeDevices)
        }
        return pairedDevices
    }

    override fun observePairedDevices(localDeviceType: DeviceType): Flow<Map<String, BluetoothDevice>> {
        return callbackFlow {

            if (!bluetoothAdapter.isEnabled) {
                // Bluetooth is not enabled
                Timber.d("Bluetooth is not enabled")
                // You can request user to enable Bluetooth here
                send(mapOf())
                return@callbackFlow
            }

            if (getPairedDevicesEndedWithError()) return@callbackFlow

            // BroadcastReceiver for changes in bonded state
            val bondStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action

                    val pairedDevices = getPairedDevices()

                    Timber.d("Updating paired devices: $pairedDevices")

                    val pairedNodes: HashMap<String, BluetoothDevice> =
                        pairedDevices.values.mapNotNull {
                            it
                        }
                            .associateBy { it.address }
                            .toMap(HashMap())
                    trySend(pairedNodes)
                    _pairedDevices.tryEmit(pairedNodes)

                    if (action == android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                        val device =
                            intent.getParcelableExtra<android.bluetooth.BluetoothDevice>(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                    }
                }
            }

            val filter = IntentFilter(android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(bondStateReceiver, filter)

            awaitClose {
                context.unregisterReceiver(bondStateReceiver)
                Timber.d("Unregistered bluetooth change receiver")
            }
        }
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                android.bluetooth.BluetoothDevice.ACTION_FOUND -> {
                    val device: android.bluetooth.BluetoothDevice? =
                        intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        // Process each discovered device
                        Timber.d("Found device: ${it.name} - ${it.address}")
                        if (nativePairedDevices.value.containsKey(it.address)) {
                            _nativeNearbyUnpairedDevices.update { currentMap ->
                                currentMap + (it.address to it)
                            }
                            val bluetoothDevice = it.toBluetoothDevice()
                            bluetoothDevice?.let {
                                _nearbyUnpairedDevices.update { currentMap ->
                                    currentMap + (it.address to it)
                                }
                            }
                            Timber.d("Paired device in range: ${it.name} - ${it.address}")
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery is complete
                    Timber.d("Discovery finished.")
                    context.unregisterReceiver(this)
                }
            }
        }
    }

    override fun startDiscovery() {
        coroutineScope.launch {
            _nativeNearbyUnpairedDevices.emit(mapOf())
            _nearbyUnpairedDevices.emit(mapOf())
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Register the BroadcastReceiver for discovery results
        val filter = IntentFilter().apply {
            addAction(android.bluetooth.BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)

        // Start discovery
        bluetoothAdapter.startDiscovery()
    }

    private suspend fun ProducerScope<Map<String, BluetoothDevice>>.getPairedDevicesEndedWithError(): Boolean {
        try {
            val pairedDevices = getPairedDevices()
            Timber.d("Obtaining paired devices ${pairedDevices}")
            val pairedNodes: HashMap<String, BluetoothDevice> =
                pairedDevices
                    .values
                    .mapNotNull { it }
                    .associateBy { it.address }
                    .toMap(HashMap())
            trySend(pairedNodes)
            _pairedDevices.emit(pairedNodes)
        } catch (e: SecurityException) {
            awaitClose()
            return true
        }
        return false
    }

    override fun getNativeBluetoothDeviceFromDeviceAddress(deviceAddress: String): android.bluetooth.BluetoothDevice? {
        return try {
            _nativePairedDevices.value[deviceAddress]
        } catch (e: NoSuchElementException) {
            e.printStackTrace()
            null
        }
    }
}