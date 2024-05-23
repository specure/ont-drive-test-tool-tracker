package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

data class TrackOverviewState(
    val tracks: List<TrackUi> = emptyList(),
    val isOnline: Boolean = false,
)
