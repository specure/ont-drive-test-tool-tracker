package com.specure.track.presentation.track_overview

import android.Manifest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specure.connectivity.domain.ConnectivityObserver
import com.specure.connectivity.domain.NetworkTracker
import com.specure.core.database.export.TracksExporter
import com.specure.core.domain.SessionStorage
import com.specure.core.domain.Temperature
import com.specure.core.domain.config.Config
import com.specure.core.domain.location.LocationTimestamp
import com.specure.core.domain.location.service.LocationServiceObserver
import com.specure.core.domain.track.TrackRepository
import com.specure.core.domain.wifi.WifiServiceObserver
import com.specure.core.presentation.service.ServiceChecker
import com.specure.core.presentation.service.temperature.TemperatureInfoReceiver
import com.specure.intercom.domain.message.TrackerAction
import com.specure.intercom.domain.server.BluetoothServerService
import com.specure.permissions.domain.PermissionHandler
import com.specure.permissions.presentation.appPermissions
import com.specure.track.domain.LocationObserver
import com.specure.track.domain.MeasurementTracker
import com.specure.track.presentation.track_overview.model.FileExportError
import com.specure.track.presentation.track_overview.model.FileExportUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@ExperimentalCoroutinesApi
class TrackOverviewViewModel(
    private val trackRepository: TrackRepository,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    connectivityObserver: ConnectivityObserver,
    private val permissionHandler: PermissionHandler,
    private val gpsLocationService: ServiceChecker,
    locationServiceObserver: LocationServiceObserver,
    wifiServiceObserver: WifiServiceObserver,
    private val locationObserver: LocationObserver,
    private val mobileNetworkObserver: NetworkTracker,
    private val temperatureInfoReceiver: TemperatureInfoReceiver,
    private val trackExporter: TracksExporter,
    private val appConfig: Config,
    private val intercomService: BluetoothServerService,
    private val measurementTracker: MeasurementTracker,
) : ViewModel() {

    var state by mutableStateOf(TrackOverviewState())
        private set

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
            trackRepository.getTracksForExport().collect { tracks ->
                updateTrackSizeForExport(tracks.size)
            }
        }

        viewModelScope.launch {
            temperatureInfoReceiver.observeTemperature().collect { temperature ->
                updateTemperature(temperature)
            }
        }

        permissionHandler.setPermissionsNeeded(
            appPermissions
        )

        connectivityObserver.observeBasicConnectivity().onEach {
            Timber.d("Online status changes - is online: $it")
            onOnlineStatusChange(it)

        }.launchIn(viewModelScope)

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
                    state = state.copy(mobileNetworkInfo = it.first())
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

    }

    private fun updateTrackSizeForExport(trackCount: Int) {
        state = state.copy(
            trackCountForExport = trackCount
        )
    }

    private fun updateTemperature(temperatureCelsius: Temperature?) {
        state = state.copy(
            currentTemperatureCelsius = temperatureCelsius
        )
    }

    fun onAction(action: TrackOverviewAction) {
        when (action) {
            TrackOverviewAction.OnSettingsClick -> logout()

            TrackOverviewAction.OnResolveLocationService -> {
                gpsLocationService.resolve()
            }

            TrackOverviewAction.OnExportToCsvClick -> {

                this.applicationScope.launch {
                    trackExporter.exportStateFlow.collect { exportState ->
                        when (exportState) {
                            is TracksExporter.ExportState.Initial -> {
                                state = state.copy(
                                    fileExport = FileExportUi(
                                        null,
                                        null,
                                        null,
                                    )
                                )
                            }

                            is TracksExporter.ExportState.Exporting -> {
                                state = state.copy(
                                    fileExport = FileExportUi(
                                        null,
                                        exportState.progress,
                                        null,
                                    )
                                )
                            }

                            is TracksExporter.ExportState.Success -> {
                                // Download completed successfully
                                val exportedFile = exportState.file
                                state = state.copy(
                                    fileExport = FileExportUi(
                                        exportState.file,
                                        100,
                                        null
                                    )
                                )
                                trackExporter.openFile(exportedFile) { exception ->
                                    state = state.copy(
                                        fileExport = FileExportUi(
                                            exportState.file,
                                            100,
                                            FileExportError.Unknown(exception.message.toString())
                                        )
                                    )
                                }
                            }

                            is TracksExporter.ExportState.NothingToExport -> {
                                state = state.copy(
                                    fileExport = FileExportUi(
                                        null,
                                        null,
                                        FileExportError.NothingToExport()
                                    )
                                )
                            }


                            is TracksExporter.ExportState.Error -> {
                                state = state.copy(
                                    fileExport = FileExportUi(
                                        null,
                                        null,
                                        FileExportError.Unknown("Unknown error")
                                    )
                                )
                            }
                        }
                    }
                }

                applicationScope.launch {
                    trackExporter.exportFile()
                }
            }
            else -> Unit
        }
    }


    override fun onCleared() {
        super.onCleared()
        temperatureInfoReceiver.unregister()
    }

    fun onEvent(event: TrackOverviewEvent) {
        when (event) {
            TrackOverviewEvent.OnUpdatePermissionStatus -> {
                permissionHandler.checkPermissionsState()
                measurementTracker.setPreparedForRemoteController(false)
                updatePermissionsState()
            }

            TrackOverviewEvent.OnUpdateServiceStatus -> {
                val isGpsEnabled = gpsLocationService.isServiceEnabled()
                val isAvailable = gpsLocationService.isServiceAvailable()

                updateGpsLocationServiceStatus(isGpsEnabled, isAvailable)
                temperatureInfoReceiver.register()
                updateAccordingConfig()
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

        if (!state.isPermissionRequired) {
            intercomService.startGattServer()
            intercomService.receivedActionFlow.onEach { action ->
                if (action is TrackerAction.StartTest) {
                    Timber.d("Received start tracking request in overview screen")
                    // TODO: start tracking screen with automated start
                }
            }.launchIn(viewModelScope)
        }
        startObservingData(state.isLocationTrackable)
    }

    private fun updateAccordingConfig() {
        val enabledSpeedTest = appConfig.isSpeedTestEnabled()
        state = state.copy(
            isSpeedTestEnabled = enabledSpeedTest
        )
    }

    fun startObservingData(isLocationTrackable: Boolean) {
        isObservingLocation.value = isLocationTrackable
    }

    fun onOnlineStatusChange(isOnline: Boolean) {
        this.state = state.copy(
            isOnline = isOnline
        )
    }

    private fun logout() {
        applicationScope.launch {
            sessionStorage.set(null)
        }
    }
}