package com.specure.intercom.domain.data

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementProgress(
    val state: MeasurementState,
    val errors: List<TestError>?,
    val appVersion: String?,
    val timestamp: Long,
)
