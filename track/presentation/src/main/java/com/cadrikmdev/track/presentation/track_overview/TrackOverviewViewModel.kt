package com.cadrikmdev.track.presentation.track_overview

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
import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.domain.track.SyncTrackScheduler
import com.cadrikmdev.core.domain.track.TrackRepository
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.core.presentation.service.temperature.TemperatureInfoReceiver
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.BuildConfig
import com.cadrikmdev.permissions.presentation.appPermissions
import com.cadrikmdev.track.presentation.track_overview.mapper.toTrackUi
import com.synaptictools.iperf.IPerf
import com.synaptictools.iperf.IPerfConfig
import com.synaptictools.iperf.IPerfResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
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
    private val mobileNetworkObserver: NetworkTracker,
    private val temperatureInfoReceiver: TemperatureInfoReceiver,
    private val applicationContext: Context,
) : ViewModel() {

    var state by mutableStateOf(TrackOverviewState())
        private set

    private val resultBuilder: StringBuilder = StringBuilder()

    private val _iPerfRequestResult: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val iPerfRequestResult: LiveData<String>
        get() = _iPerfRequestResult

    private val _iPerfSpeed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val iPerfSpeed: LiveData<String>
        get() = _iPerfSpeed

    private val _iPerfTestRunning: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }
    val iPerfTestRunning: LiveData<Boolean>
        get() = _iPerfTestRunning

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
            iPerfRequestResult.asFlow().collect {
                state = state.copy(
                    currentIperfInfoRaw = it
                )
            }
        }

        viewModelScope.launch {
            _iPerfSpeed.asFlow().collect {
                state = state.copy(
                    currentIperfSpeed = it
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

        viewModelScope.launch {
            mobileNetworkObserver.observeNetwork().collect {
                if (it.isEmpty()) {
                    state = state.copy(mobileNetworkInfo = null)
                } else {
                    state = state.copy(mobileNetworkInfo = it.first() as MobileNetworkInfo)
                }
            }
        }

        viewModelScope.launch {
            startIperf()
        }
    }

    private fun updateTemperature(temperatureCelsius: Temperature?) {
        state = state.copy(
            currentTemperatureCelsius = temperatureCelsius
        )
    }

    fun onAction(action: TrackOverviewAction) {
        when (action) {
            TrackOverviewAction.OnLogoutClick -> logout()
            TrackOverviewAction.OnStartClick -> {
                startIperf()
            }
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

    fun startIperf() {
        val hostname: String = BuildConfig.BASE_URL.toString()
//            val port = etPort.text.toString()
        if (hostname.isNotEmpty()) {
            val stream = File(applicationContext.filesDir, "iperf3.XXXXXX")

            startRequest(
                IPerfConfig(
                    hostname = hostname,
                    stream = stream.path,
                    duration = 10,
                    interval = 1,
                    download = true,
                    useUDP = false,
                    json = false,
                    debug = false,
                    maxBandwidthBitPerSecond = 20000000,
                ),
                isAsync = true
            )
        }
    }

    fun startRequest(config: IPerfConfig, isAsync: Boolean = true) {
        Timber.d("isAsync request $isAsync")
        Timber.d("start request with $config")
        _iPerfTestRunning.postValue(true)
        resultBuilder.clear()
        viewModelScope.launch {
            doStartRequest(config, isAsync)
        }
    }

    private suspend fun doStartRequest(config: IPerfConfig, isAsync: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                if (isAsync) {
                    IPerf.seCallBack {
                        success {
                            Timber.d("iPerf request done")
                            _iPerfTestRunning.postValue(false)
                        }
                        update { text ->
                            val pattern = """\[(\d+)]\s+(\d+\.\d+)-(\d+\.\d+)\s+sec\s+(\d+\.\d+)\s+(MBytes|KBytes|GBytes)\s+(\d+\.\d+)\s+(Mbits/sec|Kbits/sec|Gbits/sec)""".toRegex()

                            // Match the input string with the pattern
                            val matchResult = pattern.find(text.toString())

                            if (matchResult != null) {
                                // Extract the matched groups
                                val (index, startTime, endTime, dataSize, dataUnit, transferRate, transferUnit) = matchResult.destructured

                                // Print the extracted values
                                println("Index: $index")
                                println("Start Time: $startTime sec")
                                println("End Time: $endTime sec")
                                println("Data Size: $dataSize $dataUnit")
                                println("Transfer Rate: $transferRate $transferUnit")
                                _iPerfSpeed.postValue(transferRate)
                            } else {
                                _iPerfSpeed.postValue("-")
                            }
                            _iPerfRequestResult.postValue(resultBuilder.toString())
                            resultBuilder.append(text)


                        }
                        error { e ->
                            resultBuilder.append("\niPerf request failed:\n error: $e")
                            Timber.d("$resultBuilder")
                            _iPerfTestRunning.postValue(false)
                        }
                    }
                }
                val result = IPerf.request(config)
                if (!isAsync) {
                    when (result) {
                        is IPerfResult.Success -> {
                            Timber.d("iPerf request done")
                            resultBuilder.append(result.data)
                            _iPerfRequestResult.postValue(resultBuilder.toString())
                            _iPerfTestRunning.postValue(false)
                        }

                        is IPerfResult.Error -> {
                            Timber.d("iPerf request failed-> ${result.error}")
                            resultBuilder.append("\niPerf request failed:\n error: $result.error")
                            _iPerfTestRunning.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d("error on doStartRequest() -> ${e.message}")
            }
        }
    }
}