package com.cadrikmdev.track.presentation.active_track

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.domain.config.Config
import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.intercom.domain.message.TrackerAction
import com.cadrikmdev.permissions.domain.PermissionHandler
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
import timber.log.Timber

class ActiveTrackViewModel(
    private val measurementTracker: MeasurementTracker,
    private val permissionHandler: PermissionHandler,
    private val gpsLocationService: ServiceChecker,
    private val locationServiceObserver: LocationServiceObserver,
    private val appConfig: Config,
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

    private val hasAllPermission = MutableStateFlow(false)

    private val isLocationTrackable = MutableStateFlow(false)
    private val isLocationServiceEnabled = MutableStateFlow(false)

    private val isTracking = combine(
        shouldTrack,
        hasAllPermission,
        isLocationTrackable
    ) { shouldTrack, hasPermission, isLocationObserved ->
        shouldTrack && hasPermission && isLocationObserved
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    init {
        hasAllPermission
            .onEach { hasPermission ->
                if (hasPermission) {
                    measurementTracker.startObserving()
                } else {
                    measurementTracker.stopObserving()
                }
            }
            .launchIn(viewModelScope)

        locationServiceObserver.observeLocationServiceStatus().onEach { serviceStatus ->
            val isAvailable = gpsLocationService.isServiceAvailable()
            updateGpsLocationServiceStatus(serviceStatus.isGpsEnabled, isAvailable)
        }.launchIn(viewModelScope)

        measurementTracker.trackActions.onEach { action ->
            when (action) {
                is TrackerAction.StartTest -> onAction(ActiveTrackAction.OnStartTrackClick)
                is TrackerAction.StopTest -> onAction(ActiveTrackAction.OnStopTrackClick)
                is TrackerAction.UpdateProgress -> Unit
                null -> Unit
            }
        }.launchIn(viewModelScope)

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
                state = state.stopTracking()
                finishTrackAndLeave()
            }

            ActiveTrackAction.OnResumeTrackClick -> {
                state = state.copy(
                    shouldTrack = true,
                    isShowingFinishConfirmationDialog = false,
                )
            }

            ActiveTrackAction.OnBackClick -> {
                state = state.copy(
                    shouldTrack = false
                )
            }

            ActiveTrackAction.OnToggleTrackClick -> {
                if (!shouldTrack.value) {
                    onResumeChecks()
                }
                state = state.copy(
                    hasStartedTracking = true,
                    isShowingFinishConfirmationDialog = shouldTrack.value,
                    shouldTrack = true,
                )
            }

            ActiveTrackAction.OnStartTrackClick -> {
                Timber.d("Received Start action")
                onResumeChecks()
                state = state.copy(
                    hasStartedTracking = true,
                    isShowingFinishConfirmationDialog = false,
                    shouldTrack = true,
                )
            }

            ActiveTrackAction.OnStopTrackClick -> {
                Timber.d("Received Stop action")
                measurementTracker.setIsTracking(false)
                state = state.copy(
                    isShowingFinishConfirmationDialog = false,
                    shouldTrack = false,
                )
                finishTrack()
            }

            is ActiveTrackAction.OnTrackProcessed -> {
                finishTrackAndLeave()
            }

            else -> Unit
        }
    }

    private fun finishTrack() {
        viewModelScope.launch {
            measurementTracker.finishTrack()
            state = state.copy(
                shouldTrack = false,
                isSavingTrack = false,
                isShowingFinishConfirmationDialog = false
            )
        }
    }

    private fun finishTrackAndLeave() {
        viewModelScope.launch {
            measurementTracker.finishTrack()
            eventChannel.send(ActiveTrackEvent.TrackSaved)
            state = state.copy(
                shouldTrack = false,
                isSavingTrack = false,
                isShowingFinishConfirmationDialog = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveTrackService.isServiceActive) {
            measurementTracker.stopObserving()
        }
    }

    fun onEvent(event: ActiveTrackEvent) {
        when (event) {
            ActiveTrackEvent.OnUpdatePermissionStatus -> {
                onResumeChecks()
            }
            else -> Unit
        }
    }

    private fun onResumeChecks() {
        permissionHandler.checkPermissionsState()
        val isGpsEnabled = gpsLocationService.isServiceEnabled()
        val isAvailable = gpsLocationService.isServiceAvailable()

        updateGpsLocationServiceStatus(isGpsEnabled, isAvailable)
        updatePermissionsState()
        updateAccordingAppConfig()
    }

    private fun updateGpsLocationServiceStatus(isGpsEnabled: Boolean, isAvailable: Boolean) {
        isLocationServiceEnabled.value = isGpsEnabled && isAvailable
        isLocationTrackable.value = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && isAvailable && isGpsEnabled
    }

    private fun updatePermissionsState() {
        hasAllPermission.value = permissionHandler.getNotGrantedPermissionList().isEmpty()
        isLocationTrackable.value = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && isLocationServiceEnabled.value
    }

    private fun updateAccordingAppConfig() {
        val isSpeedTestEnabled = appConfig.isSpeedTestEnabled()
        state = state.copy(
            isSpeedTestEnabled = isSpeedTestEnabled
        )
    }
}