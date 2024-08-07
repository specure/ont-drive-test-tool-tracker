package com.cadrikmdev.intercom.data.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackerActionDto {
    @Serializable
    data object StartTest : TrackerActionDto

    @Serializable
    data object StopTest : TrackerActionDto

    @Serializable
    data class UpdateProgress(val progress: MeasurementProgress) : TrackerActionDto
}