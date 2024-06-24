package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

sealed interface TrackOverviewAction {
    data object OnStartClick : TrackOverviewAction
    data object OnLogoutClick : TrackOverviewAction
    data object OnDemoStartClick : TrackOverviewAction
    data object OnResolvePermissionClick : TrackOverviewAction
    data object OnResolveLocationService : TrackOverviewAction
    data object OnDownloadTestClick : TrackOverviewAction
    data object OnUploadTestClick : TrackOverviewAction

    data class DeleteTrack(val trackUi: TrackUi) : TrackOverviewAction
}