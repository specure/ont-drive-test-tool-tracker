package com.specure.track.domain.intercom.data

import com.cadrikmdev.intercom.domain.message.SerializableContent
import kotlinx.serialization.Serializable

@Serializable
data class MeasurementProgressContent(
    val state: MeasurementState,
    val errors: List<TestError>?,
    val appVersion: String?,
    val timestamp: Long,
) : SerializableContent()

@Serializable
data object StartTestContent : SerializableContent()

@Serializable
data object StopTestContent : SerializableContent()