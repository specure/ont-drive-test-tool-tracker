package com.cadrikmdev.intercom.domain.data

import com.cadrikmdev.core.domain.util.Error


data class MeasurementProgress(
    val state: MeasurementState,
    val error: Error,
    val timestamp: Long,
)
