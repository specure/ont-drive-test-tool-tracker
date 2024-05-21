package com.cadrikmdev.track.presentation.active_track

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.domain.locaiton.Location
import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.domain.util.Result
import com.cadrikmdev.core.presentation.ui.asUiText
import com.cadrikmdev.track.domain.LocationDataCalculator
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.active_track.service.ActiveTrackService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

class ActiveTrackViewModel(
    private val measurementTracker: MeasurementTracker,
    private val trackRepository: TrackRepository,
) : ViewModel() {

    var state by mutableStateOf(
        ActiveTrackState(
            shouldTrack = ActiveTrackService.isServiceActive && measurementTracker.isTracking.value,
            hasStartedTracking = ActiveTrackService.isServiceActive
        )
    )
        private set

    private val eventChannel = Channel<ActiveTrackEvent>()
    val events = eventChannel.receiveAsFlow()

    private val shouldTrack = snapshotFlow {
        state.shouldTrack
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, state.shouldTrack)

    private val hasLocationPermission = MutableStateFlow(false)

    private val isTracking = combine(
        shouldTrack,
        hasLocationPermission
    ) { shouldTrack, hasPermission ->
        shouldTrack && hasPermission
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        hasLocationPermission
            .onEach { hasPermission ->
                if (hasPermission) {
                    measurementTracker.startObservingLocation()
                } else {
                    measurementTracker.stopObservingLocation()
                }
            }
            .launchIn(viewModelScope)

        isTracking
            .onEach { isTracking ->
                measurementTracker.setIsTracking(isTracking)
            }
            .launchIn(viewModelScope)

        measurementTracker
            .currentLocation
            .onEach {
                state = state.copy(currentLocation = it?.location)
            }
            .launchIn(viewModelScope)

        measurementTracker
            .trackData
            .onEach {
                state = state.copy(trackData = it)
            }
            .launchIn(viewModelScope)

        measurementTracker
            .elapsedTime
            .onEach {
                state = state.copy(elapsedTime = it)
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: ActiveTrackAction) {
        when (action) {
            ActiveTrackAction.OnFinishTrackClick -> {
                state = state.copy(
                    isTrackFinished = true,
                    isSavingTrack = true,
                )
            }

            ActiveTrackAction.OnResumeTrackClick -> {
                state = state.copy(
                    shouldTrack = true
                )
            }

            ActiveTrackAction.OnBackClick -> {
                state = state.copy(
                    shouldTrack = false
                )
            }

            ActiveTrackAction.OnToggleTrackClick -> {
                state = state.copy(
                    hasStartedTracking = true,
                    shouldTrack = !state.shouldTrack
                )
            }

            is ActiveTrackAction.SubmitLocationPermissionInfo -> {
                hasLocationPermission.value = action.acceptedLocationPermission
                state = state.copy(
                    showLocationRationale = action.showLocationRationale,
                )
            }

            is ActiveTrackAction.SubmitNotificationPermissionInfo -> {
                state = state.copy(
                    showNotificationRationale = action.showNotificationRationale,
                )
            }

            is ActiveTrackAction.DismissRationaleDialog -> {
                state = state.copy(
                    showLocationRationale = false,
                    showNotificationRationale = false,
                )
            }

            is ActiveTrackAction.OnTrackProcessed -> {
                finishTrack()
            }

            else -> Unit
        }
    }

    private fun finishTrack() {
        val locations = state.trackData.locations
        if (locations.isEmpty() || locations.first().size <= 1) {
            state = state.copy(
                isSavingTrack = false
            )
            return
        }
        viewModelScope.launch {
            val track = Track(
                id = null,
                duration = state.elapsedTime,
                dateTimeUtc = ZonedDateTime.now()
                    .withZoneSameInstant(ZoneId.of("UTC")),
                distanceMeters = state.trackData.distanceMeters,
                location = state.currentLocation ?: Location(0.0, 0.0),
                maxSpeedKmh = LocationDataCalculator.getMaxSpeedKmh(locations),
                totalElevationMeters = LocationDataCalculator.getTotalElevationMeters(locations),
            )

            measurementTracker.finishTrack()

            when (val result = trackRepository.upsertTrack(track)) {
                is Result.Error -> {
                    eventChannel.send(ActiveTrackEvent.Error(result.error.asUiText()))
                }

                is Result.Success -> {
                    eventChannel.send(ActiveTrackEvent.TrackSaved)
                }
            }

            state = state.copy(
                isSavingTrack = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveTrackService.isServiceActive) {
            measurementTracker.stopObservingLocation()
        }
    }
}