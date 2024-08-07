package com.cadrikmdev.intercom.domain.server

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import com.cadrikmdev.intercom.domain.message.TrackerAction
import kotlinx.coroutines.flow.StateFlow

interface BluetoothServerService {

    val receivedActionFlow: StateFlow<TrackerAction?>

    fun startGattServer()

    fun stopGattServer()

    fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?)
}