package com.cadrikmdev.intercom.domain.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackerAction {
    @Serializable
    data class StartTest(val address: String) : TrackerAction
    @Serializable
    data class StopTest(val address: String) : TrackerAction
    @Serializable
    data class UpdateProgress(val progress: MeasurementProgress) : TrackerAction
}