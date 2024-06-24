package com.cadrikmdev.track.presentation.track_overview

import android.Manifest
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.connectivty.domain.connectivity.ConnectivityObserver
import com.cadrikmdev.core.connectivty.domain.connectivity.NetworkTracker
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkInfo
import com.cadrikmdev.core.domain.SessionStorage
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.domain.wifi.WifiServiceObserver
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.core.presentation.service.temperature.TemperatureInfoReceiver
import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.iperf.domain.IperfTestStatus
import com.cadrikmdev.iperf.presentation.IperfDownloadRunner
import com.cadrikmdev.iperf.presentation.IperfUploadRunner
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.appPermissions
import com.cadrikmdev.track.domain.LocationObserver
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.track_overview.mapper.toTrackUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TrackOverviewViewModel(
    private val trackRepository: TrackRepository,
    private val syncTrackScheduler: SyncTrackScheduler,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    private val connectivityObserver: ConnectivityObserver,
    private val permissionHandler: PermissionHandler,
    private val gpsLocationService: ServiceChecker,
    private val locationServiceObserver: LocationServiceObserver,
    private val wifiServiceObserver: WifiServiceObserver,
    private val locationObserver: LocationObserver,
    private val mobileNetworkObserver: NetworkTracker,
    private val temperatureInfoReceiver: TemperatureInfoReceiver,
    private val applicationContext: Context,
    private val measurementTracker: MeasurementTracker,
    private val iperfParser: IperfOutputParser,
) : ViewModel() {

    var state by mutableStateOf(TrackOverviewState())
        private set

    private val downloadResultBuilder: StringBuilder = StringBuilder()
    private val uploadResultBuilder: StringBuilder = StringBuilder()

    private val iperfUpload = IperfUploadRunner(applicationContext, applicationScope, iperfParser)
    private val iperfDownload = IperfDownloadRunner(applicationContext, applicationScope, iperfParser)

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

    private val _iPerfDownloadTestRunning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val iPerfDownloadTestRunning: LiveData<Boolean>
        get() = _iPerfDownloadTestRunning

    private val _iPerfUploadTestRunning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val iPerfUploadTestRunning: LiveData<Boolean>
        get() = _iPerfUploadTestRunning

    private val isObservingLocation = MutableStateFlow(false)

    private val currentLocation = isObservingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation(1000L)
            } else flowOf(null)
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    init {

        viewModelScope.launch {
            temperatureInfoReceiver.temperatureFlow.collect { temperature ->
                updateTemperature(temperature)
            }
        }

        viewModelScope.launch {
            temperatureInfoReceiver.register()
        }

        viewModelScope.launch {
            iPerfDownloadRequestResult.asFlow().collect {
                state = state.copy(
                    currentIperfDownloadInfoRaw = it
                )
            }
        }

        viewModelScope.launch {
            iPerfUploadRequestResult.asFlow().collect {
                state = state.copy(
                    currentIperfUploadInfoRaw = it
                )
            }
        }

        viewModelScope.launch {
            syncTrackScheduler.scheduleSync(
                type = SyncTrackScheduler.SyncType.FetchTracks(30.minutes)
            )
        }

        permissionHandler.setPermissionsNeeded(
            appPermissions
        )

        connectivityObserver.observeBasicConnectivity().onEach {
            Timber.d("Online status changes - is online: $it")
            onOnlineStatusChange(it)

        }.launchIn(viewModelScope)

        trackRepository.getTracks().onEach { tracks ->
            val trackUis = tracks.map { it.toTrackUi() }
            state = state.copy(tracks = trackUis)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            trackRepository.syncPendingTracks()
            trackRepository.fetchTracks()
        }

        locationServiceObserver.observeLocationServiceStatus().onEach { serviceStatus ->
            val isAvailable = gpsLocationService.isServiceAvailable()
            updateGpsLocationServiceStatus(serviceStatus.isGpsEnabled, isAvailable)
        }.launchIn(viewModelScope)

        wifiServiceObserver.observeWifiServiceEnabledStatus().onEach { serviceStatus ->
            updateWifiServiceStatusEnabled(serviceStatus)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            mobileNetworkObserver.observeNetwork().collect {
                if (it.isEmpty()) {
                    state = state.copy(mobileNetworkInfo = null)
                } else {
                    state = state.copy(mobileNetworkInfo = it.first() as MobileNetworkInfo)
                }
            }
        }

        currentLocation.onEach { location ->

            if (location == null) {
                state = state.copy(
                    location = null,
                )
            }

            location?.let {
                state = state.copy(
                    location = LocationTimestamp(
                        location,
                        System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
                    )
                )
            }
        }.launchIn(viewModelScope)

        iperfUpload.testProgressDetailsFlow.onEach {
            state = if (it.testProgress.isEmpty()) {
                state.copy(
                    currentIperfUploadSpeed = "-",
                    currentIperfUploadSpeedUnit = "",
                    isIperfUploadRunning = it.status in setOf(IperfTestStatus.RUNNING, IperfTestStatus.INITIALIZING)
                )
            } else {
                state.copy(
                    currentIperfUploadSpeed = it.testProgress.last().bandwidth.toString(),
                    currentIperfUploadSpeedUnit = it.testProgress.last().bandwidthUnit,
                    isIperfUploadRunning = it.status in setOf(IperfTestStatus.RUNNING, IperfTestStatus.INITIALIZING)
                )
            }

        }.launchIn(viewModelScope)

        iperfDownload.testProgressDetailsFlow.onEach {
            state = if (it.testProgress.isEmpty()) {
                state.copy(
                    currentIperfDownloadSpeed = "-",
                    currentIperfDownloadSpeedUnit = "",
                    isIperfDownloadRunning = it.status in setOf(IperfTestStatus.RUNNING, IperfTestStatus.INITIALIZING)
                )
            } else {
                state.copy(
                    currentIperfDownloadSpeed = it.testProgress.last().bandwidth.toString(),
                    currentIperfDownloadSpeedUnit = it.testProgress.last().bandwidthUnit,
                    isIperfDownloadRunning = it.status in setOf(IperfTestStatus.RUNNING, IperfTestStatus.INITIALIZING)
                )
            }
        }.launchIn(viewModelScope)

//        viewModelScope.launch {
//            startIperf()
//        }
    }

    private fun updateTemperature(temperatureCelsius: Temperature?) {
        state = state.copy(
            currentTemperatureCelsius = temperatureCelsius
        )
    }

    fun onAction(action: TrackOverviewAction) {
        when (action) {
            TrackOverviewAction.OnLogoutClick -> logout()
            TrackOverviewAction.OnDemoStartClick -> {
                viewModelScope.launch {
                    startIperf()
                }
            }
            is TrackOverviewAction.DeleteTrack -> {
                viewModelScope.launch {
                    trackRepository.deleteTrack(action.trackUi.id)
                }
            }

            TrackOverviewAction.OnResolveLocationService -> {
                gpsLocationService.resolve()
            }

            TrackOverviewAction.OnDownloadTestClick -> {
                viewModelScope.launch {
                    if (state.isIperfDownloadRunning) {
                        iperfDownload.stopTest()
                    } else {
                        iperfDownload.startTest()
                    }
                }
            }
            TrackOverviewAction.OnUploadTestClick -> {
                viewModelScope.launch {
                    if (state.isIperfUploadRunning) {
                        iperfUpload.stopTest()
                    } else {
                        iperfUpload.startTest()
                    }
                }
            }
            else -> Unit
        }
    }

    private fun startIperf() {
        iperfDownload.startTest()
        iperfUpload.startTest()
    }

    override fun onCleared() {
        super.onCleared()
        temperatureInfoReceiver.unregister()
    }

    fun onEvent(event: TrackOverviewEvent) {
        when (event) {
            TrackOverviewEvent.OnUpdatePermissionStatus -> {
                permissionHandler.checkPermissionsState()
                updatePermissionsState()
            }

            TrackOverviewEvent.OnUpdateServiceStatus -> {
                val isGpsEnabled = gpsLocationService.isServiceEnabled()
                val isAvailable = gpsLocationService.isServiceAvailable()

                updateGpsLocationServiceStatus(isGpsEnabled, isAvailable)
                // todo: update wifi state
            }
        }
    }

    private fun updateWifiServiceStatusEnabled(isWifiEnabled: Boolean) {
        this.state = state.copy(
            isWifiServiceEnabled = isWifiEnabled,
        )
        startObservingData(state.isLocationTrackable)
    }

    private fun updateGpsLocationServiceStatus(isGpsEnabled: Boolean, isAvailable: Boolean) {
        this.state = state.copy(
            isLocationServiceEnabled = isGpsEnabled && isAvailable,
            isLocationServiceResolvable = isAvailable,
            isLocationTrackable = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && isAvailable && isGpsEnabled
        )
        startObservingData(state.isLocationTrackable)
    }

    private fun updatePermissionsState() {
        state = state.copy(
            isPermissionRequired = permissionHandler.getNotGrantedPermissionList().isNotEmpty(),
            isLocationTrackable = (permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || permissionHandler.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) && state.isLocationServiceEnabled
        )
        startObservingData(state.isLocationTrackable)
    }

    fun startObservingData(isLocationTrackable: Boolean) {
        isObservingLocation.value = isLocationTrackable
        // TODO: if all necessary prerequisities are fullfilled then we can run measurement tracker to track values and save it to DB
    }

    fun onOnlineStatusChange(isOnline: Boolean) {
        this.state = state.copy(
            isOnline = isOnline
        )
    }

    private fun logout() {
        applicationScope.launch {
            syncTrackScheduler.cancelAllSyncs()
            trackRepository.deleteAllTracks()
            sessionStorage.set(null)
        }

    }
}