package com.cadrikmdev.intercom.data.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackerActionDto {
    @Serializable
    data class StartTest(val address: String) : TrackerActionDto
    @Serializable
    data class StopTest(val address: String) : TrackerActionDto
    @Serializable
    data class UpdateProgress(val progress: MeasurementProgress) : TrackerActionDto
}