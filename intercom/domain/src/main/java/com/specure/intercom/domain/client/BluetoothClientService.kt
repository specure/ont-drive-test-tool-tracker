package com.specure.intercom.domain.client

import com.specure.core.domain.util.Result
import com.specure.intercom.domain.message.TrackerAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface BluetoothClientService {

    val trackingDevices: MutableStateFlow<Map<String, TrackingDevice>>

    val sendActionFlow: MutableStateFlow<TrackerAction?>

    fun observeConnectedDevices(localDeviceType: DeviceType): Flow<Map<String, TrackingDevice>>

    suspend fun connectToDevice(deviceAddress: String): Result<Boolean, BluetoothError>

    /**
     * Returns true if disconnecting was successfully completed or device is already disconnected
     */
    fun disconnectFromDevice(deviceAddress: String): Boolean
}