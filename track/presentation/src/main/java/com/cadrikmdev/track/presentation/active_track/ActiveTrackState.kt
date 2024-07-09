package com.cadrikmdev.track.presentation.active_track

import com.cadrikmdev.core.domain.location.Location
import com.cadrikmdev.track.domain.TrackData
import kotlin.time.Duration

data class ActiveTrackState(
    val elapsedTime: Duration = Duration.ZERO,
    val trackData: TrackData = TrackData(),
    val shouldTrack: Boolean = false,
    val hasStartedTracking: Boolean = false,
    val currentLocation: Location? = null,
    val isTrackFinished: Boolean = false,
    val isSavingTrack: Boolean = false,
    val isShowingPauseDialog: Boolean = false,
    val isSpeedTestEnabled: Boolean = false,
)
