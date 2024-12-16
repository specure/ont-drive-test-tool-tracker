package com.specure.intercom.domain.server

import com.specure.intercom.domain.data.MeasurementProgress
import com.specure.intercom.domain.message.TrackerAction
import kotlinx.coroutines.flow.SharedFlow

interface BluetoothServerService {

    val receivedActionFlow: SharedFlow<TrackerAction?>

    fun startServer()

    fun stopServer()

    fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?)
}