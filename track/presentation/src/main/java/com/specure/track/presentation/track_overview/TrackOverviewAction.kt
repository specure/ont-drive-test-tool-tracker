package com.specure.track.presentation.track_overview

sealed interface TrackOverviewAction {
    data object OnStartClick : TrackOverviewAction
    data object OnSettingsClick : TrackOverviewAction
    data object OnAboutClick : TrackOverviewAction
    data object OnResolvePermissionClick : TrackOverviewAction
    data object OnResolveLocationService : TrackOverviewAction
    data object OnExportToCsvClick : TrackOverviewAction
}