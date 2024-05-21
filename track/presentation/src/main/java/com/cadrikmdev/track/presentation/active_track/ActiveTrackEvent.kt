package com.cadrikmdev.track.presentation.active_track

import com.cadrikmdev.core.presentation.ui.UiText

sealed interface ActiveTrackEvent {
    data class Error(val error: UiText) : ActiveTrackEvent
    data object TrackSaved : ActiveTrackEvent
}