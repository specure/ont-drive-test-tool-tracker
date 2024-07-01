package com.cadrikmdev.track.presentation.active_track

import android.Manifest
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.connectivity.domain.ConnectivityObserver
import com.cadrikmdev.connectivity.domain.NetworkTracker
import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.domain.wifi.WifiServiceObserver
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.core.presentation.service.temperature.TemperatureInfoReceiver
import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.track.domain.LocationObserver
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.active_track.service.ActiveTrackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActiveTrackViewModel(
    private val measurementTracker: MeasurementTracker,
    private val trackRepository: TrackRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val permissionHandler: PermissionHandler,
    private val gpsLocationService: ServiceChecker,
    private val locationServiceObserver: LocationServiceObserver,
    private val wifiServiceObserver: WifiServiceObserver,
    private val locationObserver: LocationObserver,
    private val mobileNetworkObserver: NetworkTracker,
    private val temperatureInfoReceiver: TemperatureInfoReceiver,
    private val applicationScope: CoroutineScope,
    private val applicationContext: Context,
    private val iperfParser: IperfOutputParser,
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

    private val _iPerfDownloadRequestResult: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val iPerfDownloadRequestResult: LiveData<String>
        get() = _iPerfDownloadRequestResult

    private val _iPerfUploadRequestResult: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val iPerfUploadRequestResult: LiveData<String>
        get() = _iPerfUploadRequestResult

    private val _iPerfDownloadSpeed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val _iPerfUploadSpeed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val _iPerfUploadSpeedUnit: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val _iPerfDownloadSpeedUnit: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

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
                    isSavingTrack = false,
                    shouldTrack = false,
                )
                finishTrack()
            }

            ActiveTrackAction.OnResumeTrackClick -> {
                state = state.copy(
                    shouldTrack = true,
                    isShowingPauseDialog = false,
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
                    isShowingPauseDialog = shouldTrack.value,
                    shouldTrack = true,
                )
            }

            is ActiveTrackAction.OnTrackProcessed -> {
                finishTrack()
            }

            else -> Unit
        }
    }

    private fun finishTrack() {
        viewModelScope.launch {
            measurementTracker.finishTrack()
            eventChannel.send(ActiveTrackEvent.TrackSaved)
            state = state.copy(
                shouldTrack = false,
                isSavingTrack = false,
                isShowingPauseDialog = false
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
                permissionHandler.checkPermissionsState()
                val isGpsEnabled = gpsLocationService.isServiceEnabled()
                val isAvailable = gpsLocationService.isServiceAvailable()

                updateGpsLocationServiceStatus(isGpsEnabled, isAvailable)
                updatePermissionsState()
            }
            else -> Unit
        }
    }

    private fun updateGpsLocationServiceStatus(isGpsEnabled: Boolean, isAvailable: Boolean) {
        isLocationServiceEnabled.value = isGpsEnabled && isAvailable
        isLocationTrackable.value = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && isAvailable && isGpsEnabled
    }

    private fun updatePermissionsState() {
        hasAllPermission.value = permissionHandler.getNotGrantedPermissionList().isEmpty()
        isLocationTrackable.value = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && isLocationServiceEnabled.value
    }
}