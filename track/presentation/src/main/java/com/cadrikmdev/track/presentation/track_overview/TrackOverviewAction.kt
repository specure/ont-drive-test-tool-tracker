package com.cadrikmdev.track.presentation.track_overview

sealed interface TrackOverviewAction {
    data object OnStartClick : TrackOverviewAction
    data object OnSettingsClick : TrackOverviewAction
    data object OnDemoStartClick : TrackOverviewAction
    data object OnResolvePermissionClick : TrackOverviewAction
    data object OnResolveLocationService : TrackOverviewAction
    data object OnDownloadTestClick : TrackOverviewAction
    data object OnUploadTestClick : TrackOverviewAction
    data object OnExportToCsvClick : TrackOverviewAction

}