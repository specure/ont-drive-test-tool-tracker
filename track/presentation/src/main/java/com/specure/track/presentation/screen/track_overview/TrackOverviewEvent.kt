package com.specure.track.presentation.screen.track_overview

sealed interface TrackOverviewEvent {
    data object OnUpdatePermissionStatus : TrackOverviewEvent
    data object OnUpdateServiceStatus : TrackOverviewEvent
}