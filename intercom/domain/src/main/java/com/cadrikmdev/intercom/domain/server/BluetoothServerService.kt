package com.cadrikmdev.intercom.domain.server

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import com.cadrikmdev.intercom.domain.message.TrackerAction
import kotlinx.coroutines.flow.SharedFlow

interface BluetoothServerService {

    val receivedActionFlow: SharedFlow<TrackerAction?>

    fun startGattServer()

    fun stopGattServer()

    fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?)
}