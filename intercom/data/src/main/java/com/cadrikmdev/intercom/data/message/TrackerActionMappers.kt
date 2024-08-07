package com.cadrikmdev.intercom.data.message

import com.cadrikmdev.intercom.domain.message.TrackerAction

fun TrackerActionDto.toTrackerAction(): TrackerAction {
    return when (this) {
        TrackerActionDto.StartTest -> TrackerAction.StartTest
        TrackerActionDto.StopTest -> TrackerAction.StopTest
        is TrackerActionDto.UpdateProgress -> TrackerAction.UpdateProgress(progress)
    }
}

fun TrackerAction.toTrackerActionDto(): TrackerActionDto {
    return when (this) {
        TrackerAction.StartTest -> TrackerActionDto.StartTest
        TrackerAction.StopTest -> TrackerActionDto.StopTest
        is TrackerAction.UpdateProgress -> TrackerActionDto.UpdateProgress(progress)
    }
}