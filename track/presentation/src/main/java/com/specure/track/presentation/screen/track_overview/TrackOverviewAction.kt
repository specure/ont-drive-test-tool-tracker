package com.specure.track.presentation.screen.track_overview

sealed interface TrackOverviewAction {
    data object OnStartClick : TrackOverviewAction
    data object OnSettingsClick : TrackOverviewAction
    data object OnAboutClick : TrackOverviewAction
    data object OnResolvePermissionClick : TrackOverviewAction
    data object OnResolveLocationService : TrackOverviewAction
    data object OnExportToCsvClick : TrackOverviewAction
}