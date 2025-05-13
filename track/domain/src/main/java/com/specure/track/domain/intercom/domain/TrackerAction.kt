package com.specure.track.domain.intercom.domain

//import com.specure.track.domain.intercom.data.MeasurementProgress
import kotlinx.serialization.Serializable

@Serializable
sealed interface TrackerAction {
    @Serializable
    data class StartTest(val address: String) : TrackerAction

    @Serializable
    data class StopTest(val address: String) : TrackerAction
//    @Serializable
//    data class UpdateProgress(val progress: MeasurementProgress) : TrackerAction
}