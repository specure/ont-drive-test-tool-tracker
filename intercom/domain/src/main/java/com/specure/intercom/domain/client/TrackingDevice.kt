package com.specure.intercom.domain.client

data class TrackingDevice(
    val name: String,
    val address: String,
    val status: String,
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
        return this.status.contains("error", true)
    }

    fun isTheSameStatus(otherDevice: TrackingDevice): Boolean {
        return this.status == otherDevice.status
    }
}