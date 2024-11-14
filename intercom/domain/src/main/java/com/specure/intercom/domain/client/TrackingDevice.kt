package com.specure.intercom.domain.client

import com.specure.intercom.domain.data.MeasurementState

data class TrackingDevice(
    val name: String,
    val address: String,
    val status: MeasurementState,
    val connected: Boolean,
    val deviceAppVersion: String,
    val updateTimestamp: Long,
) {
    fun isStateChangedOnTheSameDevice(otherDevice: TrackingDevice): Boolean {
        return isTheSameDevice(otherDevice) && !isTheSameStatus(otherDevice)
    }

    fun isTheSameDevice(otherDevice: TrackingDevice): Boolean {
        return this.address == otherDevice.address
    }

    fun isErrorState(): Boolean {
        return this.status in listOf(MeasurementState.ERROR)
    }

    fun isSpeedTestErrorState(): Boolean {
        return this.status in listOf(MeasurementState.SPEEDTEST_ERROR)
    }

    fun isTheSameStatus(otherDevice: TrackingDevice): Boolean {
        return this.status == otherDevice.status
    }
}