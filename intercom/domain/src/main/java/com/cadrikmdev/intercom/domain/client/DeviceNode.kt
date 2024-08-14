package com.cadrikmdev.intercom.domain.client

import com.cadrikmdev.intercom.domain.data.MeasurementState

data class DeviceNode(
    val address: String,
    val displayName: String,
    val connected: Boolean = false,
    val status: MeasurementState = MeasurementState.UNKNOWN,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
)
