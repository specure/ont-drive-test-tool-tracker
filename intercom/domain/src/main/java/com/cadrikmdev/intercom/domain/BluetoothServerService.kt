package com.cadrikmdev.intercom.domain

import com.cadrikmdev.intercom.domain.data.MeasurementProgress

interface BluetoothServerService {

    fun startGattServer()

    fun stopGattServer()

    fun setMeasurementProgressCallback(statusUpdate: () -> MeasurementProgress?)
}