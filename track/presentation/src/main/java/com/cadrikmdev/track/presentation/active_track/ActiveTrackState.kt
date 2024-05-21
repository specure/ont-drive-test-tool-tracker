package com.cadrikmdev.track.presentation.active_track

import com.cadrikmdev.core.domain.locaiton.Location
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
    val showLocationRationale: Boolean = false,
    val showNotificationRationale: Boolean = false,
)