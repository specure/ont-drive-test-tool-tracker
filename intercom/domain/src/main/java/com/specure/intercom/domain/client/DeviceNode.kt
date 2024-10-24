package com.specure.intercom.domain.client

import com.specure.intercom.domain.data.MeasurementState

data class DeviceNode(
    val address: String,
    val displayName: String,
    val connected: Boolean = false,
    val status: MeasurementState = MeasurementState.UNKNOWN,
    val deviceAppVersion: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
)
