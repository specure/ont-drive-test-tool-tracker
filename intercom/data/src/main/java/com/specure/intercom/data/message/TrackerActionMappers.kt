package com.specure.intercom.data.message

import com.specure.intercom.domain.message.TrackerAction

fun TrackerActionDto.toTrackerAction(): TrackerAction {
    return when (this) {
        is TrackerActionDto.StartTest -> TrackerAction.StartTest(address)
        is TrackerActionDto.StopTest -> TrackerAction.StopTest(address)
        is TrackerActionDto.UpdateProgress -> TrackerAction.UpdateProgress(progress)
    }
}

fun TrackerAction.toTrackerActionDto(): TrackerActionDto {
    return when (this) {
        is TrackerAction.StartTest -> TrackerActionDto.StartTest(address)
        is TrackerAction.StopTest -> TrackerActionDto.StopTest(address)
        is TrackerAction.UpdateProgress -> TrackerActionDto.UpdateProgress(progress)
    }
}