package com.cadrikmdev.intercom.domain.data

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementProgress(
    val state: MeasurementState,
    val error: String?,
    val timestamp: Long,
)
