package com.cadrikmdev.track.presentation.track_overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.connectivty.domain.connectivity.ConnectivityObserver
import com.cadrikmdev.core.domain.SessionStorage
import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.appPermissions
import com.cadrikmdev.track.presentation.track_overview.mapper.toTrackUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

class TrackOverviewViewModel(
    private val trackRepository: TrackRepository,
    private val syncTrackScheduler: SyncTrackScheduler,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    private val connectivityObserver: ConnectivityObserver,
    private val permissionHandler: PermissionHandler,
    private val gpsLocationService: ServiceChecker,
    private val locationServiceObserver: LocationServiceObserver,
) : ViewModel() {

    var state by mutableStateOf(TrackOverviewState())
        private set

    init {
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

        viewModelScope.launch {
            locationServiceObserver.observeLocationServiceStatus().collect { serviceStatus ->
                val isAvailable = gpsLocationService.isServiceAvailable()
                updateGpsLocationServiceStatus(serviceStatus.isGpsEnabled, isAvailable)
            }
        }
    }

    fun onAction(action: TrackOverviewAction) {
        when (action) {
            TrackOverviewAction.OnLogoutClick -> logout()
            TrackOverviewAction.OnStartClick -> Unit
            is TrackOverviewAction.DeleteTrack -> {
                viewModelScope.launch {
                    trackRepository.deleteTrack(action.trackUi.id)
                }
            }

            TrackOverviewAction.OnResolveLocationService -> {
                gpsLocationService.resolve()
            }

            else -> Unit
        }
    }

    fun onEvent(event: TrackOverviewEvent) {
        when (event) {
            TrackOverviewEvent.OnUpdatePermissionStatus -> {
                permissionHandler.checkPermissionsState()
                updatePermissionsState()
            }

            TrackOverviewEvent.OnUpdateLocationServiceStatus -> {
                val isGpsEnabled = gpsLocationService.isServiceEnabled()
                val isAvailable = gpsLocationService.isServiceAvailable()

                updateGpsLocationServiceStatus(isGpsEnabled, isAvailable)
            }
        }
    }

    private fun updateGpsLocationServiceStatus(isGpsEnabled: Boolean, isAvailable: Boolean) {
        this.state = state.copy(
            isLocationServiceEnabled = isGpsEnabled && isAvailable,
            isLocationServiceResolvable = isAvailable
        )
    }

    private fun updatePermissionsState() {
        state = state.copy(
            isPermissionRequired = permissionHandler.getNotGrantedPermissionList().isNotEmpty()
        )
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