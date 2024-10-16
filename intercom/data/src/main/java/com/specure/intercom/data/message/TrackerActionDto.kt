package com.specure.intercom.data.message

import com.specure.intercom.domain.data.MeasurementProgress
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed class TrackerActionDto {
    @Serializable
    @SerialName("StartTest")
    data class StartTest(val address: String) : TrackerActionDto()
    @Serializable
    @SerialName("StopTest")
    data class StopTest(val address: String) : TrackerActionDto()
    @Serializable
    @SerialName("UpdateProgress")
    data class UpdateProgress(val progress: MeasurementProgress) : TrackerActionDto()
}