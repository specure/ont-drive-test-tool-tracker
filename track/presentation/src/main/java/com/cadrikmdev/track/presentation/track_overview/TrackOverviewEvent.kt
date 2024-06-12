package com.cadrikmdev.track.presentation.track_overview

sealed interface TrackOverviewEvent {
    data object OnUpdatePermissionStatus : TrackOverviewEvent
    data object OnUpdateServiceStatus : TrackOverviewEvent
}