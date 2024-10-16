package com.specure.track.presentation.active_track

import com.specure.core.presentation.ui.UiText

sealed interface ActiveTrackEvent {
    data class Error(val error: UiText) : ActiveTrackEvent
    data object TrackSaved : ActiveTrackEvent
    data object OnUpdatePermissionStatus : ActiveTrackEvent
}