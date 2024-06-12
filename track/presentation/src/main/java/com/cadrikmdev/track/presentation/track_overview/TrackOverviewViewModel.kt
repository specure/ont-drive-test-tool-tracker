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
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.core.presentation.service.temperature.TemperatureInfoReceiver
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.BuildConfig
import com.cadrikmdev.permissions.presentation.appPermissions
import com.cadrikmdev.track.domain.LocationObserver
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.track_overview.mapper.toTrackUi
import com.synaptictools.iperf.IPerf
import com.synaptictools.iperf.IPerfConfig
import com.synaptictools.iperf.IPerfResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import kotlin.time.Duration
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
    private val locationObserver: LocationObserver,
    private val mobileNetworkObserver: NetworkTracker,
    private val temperatureInfoReceiver: TemperatureInfoReceiver,
    private val applicationContext: Context,
    private val measurementTracker: MeasurementTracker,
) : ViewModel() {

    var state by mutableStateOf(TrackOverviewState())
        private set

    private val downloadResultBuilder: StringBuilder = StringBuilder()
    private val uploadResultBuilder: StringBuilder = StringBuilder()

    private val iperfUpload = IPerf
    private val iperfDownload = com.cadrikmdev.iperf.IPerf

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

    val iPerfDownloadSpeed: LiveData<String>
        get() = _iPerfDownloadSpeed

    private val _iPerfUploadSpeed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val iPerfUploadSpeed: LiveData<String>
        get() = _iPerfUploadSpeed


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
            _iPerfDownloadSpeed.asFlow().collect {
                state = state.copy(
                    currentIperfDownloadSpeed = it
                )
            }
        }

        viewModelScope.launch {
            _iPerfUploadSpeed.asFlow().collect {
                state = state.copy(
                    currentIperfUploadSpeed = it
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

    private fun updateTemperature(temperatureCelsius: Temperature?) {
        state = state.copy(
            currentTemperatureCelsius = temperatureCelsius
        )
    }

    fun onAction(action: TrackOverviewAction) {
        when (action) {
            TrackOverviewAction.OnLogoutClick -> logout()
            TrackOverviewAction.OnStartClick -> {
                viewModelScope.launch {
                    startIperfDownload()
                    startIperfUpload()
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

    fun startIperfDownload() {
        val hostname: String = BuildConfig.BASE_URL
//            val port = etPort.text.toString()
        if (hostname.isNotEmpty()) {
            val stream = File(applicationContext.filesDir, "iperf3.DXXXXXX")

            startDownloadRequest(
                com.cadrikmdev.iperf.IPerfConfig(
                    hostname = hostname,
                    stream = stream.path,
                    port = 5201,
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

    fun startIperfUpload() {
        val hostname: String = BuildConfig.BASE_URL
//            val port = etPort.text.toString()
        if (hostname.isNotEmpty()) {
            val stream = File(applicationContext.filesDir, "iperf3.UXXXXXX")

            startUploadRequest(
                IPerfConfig(
                    hostname = hostname,
                    stream = stream.path,
                    duration = 10,
                    interval = 1,
                    port = 5202,
                    download = false,
                    useUDP = false,
                    json = false,
                    debug = false,
                    maxBandwidthBitPerSecond = 2000000,
                ),
                isAsync = true
            )
        }
    }

    fun startDownloadRequest(config: com.cadrikmdev.iperf.IPerfConfig, isAsync: Boolean = true) {
        Timber.d("isAsync request $isAsync")
        Timber.d("start request with $config")
        _iPerfDownloadTestRunning.postValue(true)
        downloadResultBuilder.clear()
        viewModelScope.launch {
            doStartDownloadRequest(config, isAsync)
        }
    }

    fun startUploadRequest(config: IPerfConfig, isAsync: Boolean = true) {
        Timber.d("isAsync request $isAsync")
        Timber.d("start request with $config")
        _iPerfUploadTestRunning.postValue(true)
        uploadResultBuilder.clear()
        viewModelScope.launch {
            doStartUploadRequest(config, isAsync)
        }
    }

    private suspend fun doStartDownloadRequest(config: com.cadrikmdev.iperf.IPerfConfig, isAsync: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                if (isAsync) {
                    iperfDownload.seCallBack {
                        success {
                            Timber.d("iPerf download request done")
                            _iPerfDownloadTestRunning.postValue(false)
                        }
                        update { text ->
                            val pattern = """\[(\d+)]\s+(\d+\.\d+)-(\d+\.\d+)\s+sec\s+(\d+\.\d+)\s+(MBytes|KBytes|GBytes)\s+(\d+\.\d+)\s+(Mbits/sec|Kbits/sec|Gbits/sec)""".toRegex()

                            // Match the input string with the pattern
                            val matchResult = pattern.find(text.toString())

                            if (matchResult != null) {
                                // Extract the matched groups
                                val (index, startTime, endTime, dataSize, dataUnit, transferRate, transferUnit) = matchResult.destructured

                                _iPerfDownloadSpeed.postValue(transferRate)
                            } else {
                                _iPerfDownloadSpeed.postValue("-")
                            }
                            _iPerfDownloadRequestResult.postValue("D ${downloadResultBuilder.toString()}")
                            downloadResultBuilder.append(text)


                        }
                        error { e ->
                            downloadResultBuilder.append("\niPerf download request failed:\n error: $e")
                            Timber.d("D $downloadResultBuilder")
                            _iPerfDownloadTestRunning.postValue(false)
                        }
                    }
                }
                Timber.d("IPERF DOWNLOAD: $iperfDownload")
                val result = iperfDownload.request(config)
                if (!isAsync) {
                    when (result) {
                        is com.cadrikmdev.iperf.IPerfResult.Success -> {
                            Timber.d("iPerf download request done")
                            downloadResultBuilder.append(result.data)
                            _iPerfDownloadRequestResult.postValue(downloadResultBuilder.toString())
                            _iPerfDownloadTestRunning.postValue(false)
                        }

                        is com.cadrikmdev.iperf.IPerfResult.Error -> {
                            Timber.d("iPerf download request failed-> ${result.error}")
                            downloadResultBuilder.append("\niPerf download request failed:\n error: $result.error")
                            _iPerfDownloadTestRunning.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d("error on download doStartRequest() -> ${e.message}")
            }
        }
    }

    private suspend fun doStartUploadRequest(config: IPerfConfig, isAsync: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                if (isAsync) {
                    iperfUpload.seCallBack {
                        success {
                            Timber.d("iPerf upload request done")
                            _iPerfUploadTestRunning.postValue(false)
                        }
                        update { text ->
                            val pattern = """\[(\d+)\]\s+(\d+\.\d+)-(\d+\.\d+)\s+sec\s+(\d+)\s+(MBytes|KBytes|GBytes)\s+(\d+\.\d+)\s+(Mbits/sec|Kbits/sec|Gbits/sec)\s+(\d+)\s+(\d+)\s+(MBytes|KBytes|GBytes)""".toRegex()
                            // Match the input string with the pattern
                            val matchResult = pattern.find(text.toString())

                            if (matchResult != null) {
                                // Extract the matched groups
                                val (id, startTime, endTime, data, dataUnit, speed, speedUnit, errors, retrans, retransUnit) = matchResult.destructured

                                _iPerfUploadSpeed.postValue(speed)
                            } else {
                                _iPerfUploadSpeed.postValue("-")
                            }
                            _iPerfUploadRequestResult.postValue("${uploadResultBuilder.toString()}")
                            uploadResultBuilder.append("U $text")


                        }
                        error { e ->
                            uploadResultBuilder.append("\niPerf upload request failed:\n error: $e")
                            Timber.d("U $uploadResultBuilder")
                            _iPerfUploadTestRunning.postValue(false)
                        }
                    }
                }
                Timber.d("IPERF UPLOAD: $iperfUpload")
                val result = iperfUpload.request(config)
                if (!isAsync) {
                    when (result) {
                        is IPerfResult.Success -> {
                            Timber.d("iPerf upload request done")
                            uploadResultBuilder.append(result.data)
                            _iPerfUploadRequestResult.postValue("U ${uploadResultBuilder.toString()}")
                            _iPerfUploadTestRunning.postValue(false)
                        }

                        is IPerfResult.Error -> {
                            Timber.d("iPerf upload request failed-> ${result.error}")
                            uploadResultBuilder.append("\niPerf upload request failed:\n error: $result.error")
                            _iPerfUploadTestRunning.postValue(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.d("error on doStartRequest() -> ${e.message}")
            }
        }
    }
}