package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

sealed interface TrackOverviewAction {
    data object OnStartClick : TrackOverviewAction
    data object OnLogoutClick : TrackOverviewAction
    data object OnResolvePermissionClick : TrackOverviewAction


    data class DeleteTrack(val trackUi: TrackUi) : TrackOverviewAction
}