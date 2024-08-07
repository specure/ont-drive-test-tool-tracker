package com.cadrikmdev.intercom.domain.message

import com.cadrikmdev.intercom.domain.data.MeasurementProgress
import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackerAction {
    @Serializable
    data object StartTest : TrackerAction

    @Serializable
    data object StopTest : TrackerAction

    @Serializable
    data class UpdateProgress(val progress: MeasurementProgress) : TrackerAction
}