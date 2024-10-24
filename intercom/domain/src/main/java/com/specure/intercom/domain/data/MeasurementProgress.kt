package com.specure.intercom.domain.data

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementProgress(
    val state: MeasurementState,
    val error: String?,
    val appVersion: String?,
    val timestamp: Long,
)
