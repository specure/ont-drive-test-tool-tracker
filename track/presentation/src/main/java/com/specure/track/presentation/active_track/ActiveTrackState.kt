package com.specure.track.presentation.active_track

import com.specure.core.domain.location.Location
import com.specure.track.domain.TrackData
import kotlin.time.Duration

data class ActiveTrackState(
    val elapsedTime: Duration = Duration.ZERO,
    /**
     *  Should be tracking screen open and displayed correct button icon (also from service to killed app)
     */
    val shouldTrack: Boolean = false,
    /**
     * User pressed the button and tracking should start
     */
    val hasStartedTracking: Boolean = false,
    val currentLocation: Location? = null,
    /**
     * user pressed the stop button and confirmed finish
     */
    val isTrackFinished: Boolean = false,
    val isSavingTrack: Boolean = false,
    val isShowingFinishConfirmationDialog: Boolean = false,
    val isSpeedTestEnabled: Boolean = false,
    /**
     *  only mirror of what is produced in @see MeasurementTracker
     */
    val trackData: TrackData = TrackData(),
) {

    fun stopTracking(): ActiveTrackState {
        return this.copy(
            isSavingTrack = false,
            shouldTrack = false,
            isTrackFinished = true,
        )
    }
}

