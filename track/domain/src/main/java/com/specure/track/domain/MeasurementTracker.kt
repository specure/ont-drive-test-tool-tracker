@file:OptIn(ExperimentalCoroutinesApi::class)

package com.specure.track.domain

import com.cadrikmdev.intercom.domain.data.MessageContent
import com.cadrikmdev.intercom.domain.message.MessageProcessor
import com.cadrikmdev.intercom.domain.message.MessageWrapper
import com.cadrikmdev.intercom.domain.server.BluetoothServerService
import com.specure.connectivity.domain.ConnectivityObserver
import com.specure.connectivity.domain.NetworkTracker
import com.specure.connectivity.domain.TransportType
import com.specure.connectivity.domain.mobile.MobileNetworkInfo
import com.specure.core.domain.Timer
import com.specure.core.domain.config.Config
import com.specure.core.domain.location.LocationTimestamp
import com.specure.core.domain.package_info.PackageInfoProvider
import com.specure.core.domain.track.TemperatureInfoObserver
import com.specure.core.domain.track.Track
import com.specure.core.domain.track.TrackRepository
import com.specure.iperf.domain.IperfRunner
import com.specure.iperf.domain.IperfTest
import com.specure.iperf.domain.IperfTestStatus
import com.specure.track.domain.intercom.data.MeasurementProgressContent
import com.specure.track.domain.intercom.data.MeasurementState
import com.specure.track.domain.intercom.data.StartTestContent
import com.specure.track.domain.intercom.data.StopTestContent
import com.specure.track.domain.intercom.data.TestError
import com.specure.track.domain.intercom.domain.TrackerAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val INTERVAL_CORRECTION_THRESHOLD_MILLIS = 200

class MeasurementTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
    private val temperatureInfoReceiver: TemperatureInfoObserver,
    private val mobileNetworkTracker: NetworkTracker,
    private val iperfDownloadRunner: IperfRunner,
    private val iperfUploadRunner: IperfRunner,
    private val trackRepository: TrackRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val intercomService: BluetoothServerService,
    private val messageProcessor: MessageProcessor,
    private val packageInfoProvider: PackageInfoProvider,
    private val appConfig: Config,
) {
    private var latestSavedValueTimestamp: Long = 0
    private val _trackData = MutableStateFlow(TrackData())
    val trackData = _trackData.asStateFlow()

    private val _trackActions = MutableSharedFlow<TrackerAction?>(replay = 0)
    val trackActions = _trackActions.asSharedFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    private var internetConnectivityJob: Job? = null

    private var isPreparedForRemoteController = false

    val currentLocation = isObservingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation(1000)
            } else flowOf()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    init {
        startIntercomServices()

        applicationScope.launch {
            temperatureInfoReceiver.observeTemperature().collect { temperature ->
                _trackData.update {
                    it.copy(
                        temperature = temperature
                    )
                }
            }
        }

        applicationScope.launch {
            mobileNetworkTracker.observeNetwork().collect { networkInfo ->
                _trackData.update {
                    it.copy(
                        networkInfo = networkInfo.firstOrNull()
                    )
                }
            }
        }


        _isTracking
            .onEach { isTracking ->
                if (!isTracking) {
                    clearSavedTime()
                    val newList = buildList {
                        addAll(trackData.value.locations)
                    }.toList()
                    _trackData.update {
                        it.copy(
                            locations = newList
                        )
                    }
                    iperfUploadRunner.stopTest()
                    iperfDownloadRunner.stopTest()
                    applicationScope.launch {
                        temperatureInfoReceiver.unregister()
                    }
                } else {
                    applicationScope.launch {
                        temperatureInfoReceiver.register()
                    }
                    if (appConfig.isSpeedTestEnabled()) {
                        iperfUploadRunner.startTest()
                        iperfDownloadRunner.startTest()
                    }
                }
            }
            .flatMapLatest { isTracking ->
                if (isTracking) {
                    Timer.timeAndEmit()
                } else {
                    flowOf()
                }
            }
            .onEach {
                _elapsedTime.value += it
                _trackData.update { data ->
                    data.copy(
                        duration = _elapsedTime.value
                    )
                }
                if (isTimeToSaveNewLog()) {
                    println("Saving log record ${appConfig.getTrackingLogIntervalSeconds()}")
                    saveCurrentTrackData(_trackData.value)
                    setNewSavedTime()
                }
            }
            /**
             * Basically could be replaced by "sample" - but sometimes there will not be the latest values, but it is not probably necessary
             * sample is more about catching the state at a regular interval, potentially missing out on recent items that were emitted just after the interval.
             * throttleLatest ensures that you get the most recent item within each time window, but only emits after the window ends, which might include items that sample could miss or exclude.
             */
//            .throttleLatest(appConfig.getTrackingLogIntervalSeconds().toDuration(DurationUnit.SECONDS).inWholeMilliseconds)
//            .sample(appConfig.getTrackingLogIntervalSeconds().toDuration(DurationUnit.SECONDS).inWholeMilliseconds)
//            .onEach {
//                println("Saving log record ${appConfig.getTrackingLogIntervalSeconds()}")
//                if ()
//                saveCurrentTrackData(_trackData.value)
//            }
            .launchIn(applicationScope)



        currentLocation
            .filterNotNull()
            .combineTransform(_isTracking) { location, isTracking ->
                if (isTracking) {
                    emit(location)
                }
            }
            .zip(_elapsedTime) { location, elapsedTime ->
                LocationTimestamp(
                    location = location,
                    durationTimestamp = elapsedTime
                )
            }
            .onEach { locationWithDetails ->
                val newLocationsList = listOf(locationWithDetails.location)

                _trackData.update {
                    it.copy(
                        locations = newLocationsList
                    )
                }
            }
            .launchIn(applicationScope)

        iperfUploadRunner.testProgressDetailsFlow.onEach { testState ->
            val updatedTestStatus = updateStatusIfDenied(testState)
            _trackData.update {
                it.copy(
                    iperfTestUpload = updatedTestStatus
                )
            }
        }.launchIn(applicationScope)

        iperfDownloadRunner.testProgressDetailsFlow.onEach { testState ->
            val updatedTestStatus = updateStatusIfDenied(testState)
            _trackData.update {
                it.copy(
                    iperfTestDownload = updatedTestStatus
                )
            }
        }.launchIn(applicationScope)
    }

    private fun setNewSavedTime() {
        this.latestSavedValueTimestamp = System.currentTimeMillis()
    }

    private fun clearSavedTime() {
        this.latestSavedValueTimestamp = 0
    }

    private fun isTimeToSaveNewLog() =
        System.currentTimeMillis() - latestSavedValueTimestamp > appConfig.getTrackingLogIntervalSeconds()
            .toDuration(DurationUnit.SECONDS).inWholeMilliseconds - INTERVAL_CORRECTION_THRESHOLD_MILLIS

    fun setIsTracking(isTracking: Boolean) {
        this._isTracking.value = isTracking
    }

    fun setPreparedForRemoteController(isPrepared: Boolean) {
        this.isPreparedForRemoteController = isPrepared
    }

    private fun updateStatusIfDenied(testState: IperfTest): IperfTest {
        val isSpeedTestEnabled = appConfig.isSpeedTestEnabled()
        println("TEST STATE ${testState.direction}: ${testState.status} enabled: ${appConfig.isSpeedTestEnabled()}")
        val updatedTestStatus = testState.copy(
            status = if (isSpeedTestEnabled) testState.status else IperfTestStatus.DISABLED
        )
        return updatedTestStatus
    }

    fun startIntercomServices() {
        applicationScope.launch {
            intercomService.startServer()
            intercomService.setMeasurementProgressCallback {
                MessageContent(
                    content = MeasurementProgressContent(
                        state = extractState(trackData.value),
                        errors = extractErrors(trackData.value),
                        timestamp = System.currentTimeMillis(),
                        appVersion = packageInfoProvider.versionName
                    ),
                    timestamp = System.currentTimeMillis()
                )
            }

            messageProcessor.receivedMessageFlow.onEach { message ->
                val receivedMessage = message as MessageWrapper.SendMessage
                when (receivedMessage.content.content) {
                    is StartTestContent -> {
                        stopObserving()
                        clearData()
                        startObserving()
                        _isTracking.emit(true)
                        _trackActions.emit(TrackerAction.StartTest(""))
                        println("Emitting start action in Tracker")
                    }

                    is StopTestContent -> {
                        _isTracking.emit(false)
                        _trackActions.emit(TrackerAction.StopTest(""))
                        // we can clear all data, because they are already in DB
                        clearData()
                        println("Emitting stop action in Tracker")
                    }

                    else -> {
                        println("Unable to process received action")
                    }
                }
            }.launchIn(applicationScope)
        }
    }


    private fun extractState(trackData: TrackData): MeasurementState {
        return if (isPreparedForRemoteController) {
            if (trackData.isSpeedTestError()) {
                MeasurementState.SPEEDTEST_ERROR
                // TODO: handle also other type of errors
//                    MeasurementState.ERROR
            } else {
                if (isTracking.value) {
                    MeasurementState.RUNNING
                } else {
                    MeasurementState.IDLE
                }
            }
        } else {
            MeasurementState.NOT_ACTIVATED
        }
    }

    private fun extractErrors(trackData: TrackData): List<TestError>? {
        return if (isPreparedForRemoteController) {
            if (trackData.isSpeedTestError()) {
                val errors = mutableListOf<TestError>()
                if (trackData.isUploadSpeedTestError()) {
                    errors += TestError.UPLOAD_TEST_ERROR
                }
                if (trackData.isDownloadSpeedTestError()) {
                    errors += TestError.DOWNLOAD_TEST_ERROR
                }
//                errors += TestError.UNKNOWN_ERROR
                errors
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun startObservingLocation() {
        isObservingLocation.value = true
    }

    private fun stopObservingLocation() {
        isObservingLocation.value = false
    }

    fun startObserving() {
        updateConfiguration()
        startObservingTemperature()
        startObservingLocation()
        startObservingConnectivity()
    }

    fun stopObserving() {
        stopObservingLocation()
        stopObservingTemperature()
        stopObservingConnectivity()
    }

    private fun stopObservingTemperature() {
        applicationScope.launch {
            temperatureInfoReceiver.unregister()
        }
    }

    private fun startObservingTemperature() {
        applicationScope.launch {
            temperatureInfoReceiver.register()
        }
    }

    private fun startObservingConnectivity() {
        if (internetConnectivityJob?.isActive != true) {
            internetConnectivityJob = applicationScope.launch {
                connectivityObserver.observeBasicConnectivity().onEach { connected ->
                    println("Internet connectivity changed to $connected")
                    _trackData.update {
                        it.copy(
                            internetConnectionConnected = connected
                        )
                    }
                }.launchIn(this)
            }
        }
    }

    private fun stopObservingConnectivity() {
        internetConnectivityJob?.cancel()
        internetConnectivityJob = null
    }

    private fun updateConfiguration() {
        _trackData.update {
            it.copy(
                isSpeedTestEnabled = appConfig.isSpeedTestEnabled()
            )
        }
    }

    fun clearData() {
        _elapsedTime.value = Duration.ZERO
        _trackData.value = TrackData()
        startObserving()
    }

    fun finishTrack() {
        stopObserving()
        setIsTracking(false)
        clearData()
    }

    private suspend fun saveCurrentTrackData(trackData: TrackData) {
        withContext(Dispatchers.IO) {
            val currentMillis = System.currentTimeMillis()
            val isMobileNetworkActive = trackData.networkInfo?.type == TransportType.CELLULAR
            val mobileNetworkInfo =
                if (isMobileNetworkActive) trackData.networkInfo as MobileNetworkInfo else null
            print("InternetConnection: ${trackData.internetConnectionConnected}")
            val track = Track(
                durationMillis = trackData.duration.inWholeMilliseconds,
                timestamp = currentMillis.formatMillisToDateString(),
                timestampRaw = currentMillis,
                downloadSpeedMegaBitsPerSec = trackData.getDownloadSpeedInMbitsPerSec().toString(),
                downloadSpeed = if (trackData.isSpeedTestEnabled) trackData.iperfTestDownload?.testProgress?.lastOrNull()?.bandwidth else null,
                downloadSpeedUnit = if (trackData.isSpeedTestEnabled) trackData.iperfTestDownload?.testProgress?.lastOrNull()?.bandwidthUnit else null,
                downloadSpeedTestState = if (trackData.isSpeedTestEnabled) trackData.iperfTestDownload?.status?.toString() else IperfTestStatus.DISABLED.toString(),
                downloadSpeedTestError = if (trackData.isSpeedTestEnabled) {
                    if (trackData.iperfTestDownload?.error?.lastOrNull() != null) "${trackData.iperfTestDownload.error.lastOrNull()?.timestamp?.formatMillisToDateString()} ${trackData.iperfTestDownload.error.lastOrNull()?.error}"
                    else null
                } else null,
                downloadSpeedTestTimestamp = if (trackData.isSpeedTestEnabled) trackData.iperfTestDownload?.testProgress?.lastOrNull()?.timestampMillis?.formatMillisToDateString() else null,
                downloadSpeedTestTimestampRaw = if (trackData.isSpeedTestEnabled) trackData.iperfTestDownload?.testProgress?.lastOrNull()?.timestampMillis else null,
                uploadSpeedMegaBitsPerSec = trackData.getUploadSpeedInMbitsPerSec().toString(),
                uploadSpeed = if (trackData.isSpeedTestEnabled) trackData.iperfTestUpload?.testProgress?.lastOrNull()?.bandwidth else null,
                uploadSpeedUnit = if (trackData.isSpeedTestEnabled) trackData.iperfTestUpload?.testProgress?.lastOrNull()?.bandwidthUnit else null,
                uploadSpeedTestState = if (trackData.isSpeedTestEnabled) trackData.iperfTestUpload?.status?.toString() else IperfTestStatus.DISABLED.toString(),
                uploadSpeedTestError = if (trackData.isSpeedTestEnabled) {
                    if (trackData.iperfTestUpload?.error?.lastOrNull() != null) "${trackData.iperfTestUpload.error.lastOrNull()?.timestamp?.formatMillisToDateString()} ${trackData.iperfTestUpload.error.lastOrNull()?.error}"
                    else null
                } else null,
                uploadSpeedTestTimestamp = if (trackData.isSpeedTestEnabled) trackData.iperfTestUpload?.testProgress?.lastOrNull()?.timestampMillis?.formatMillisToDateString() else null,
                uploadSpeedTestTimestampRaw = if (trackData.isSpeedTestEnabled) trackData.iperfTestUpload?.testProgress?.lastOrNull()?.timestampMillis else null,
                latitude = trackData.locations.lastOrNull()?.location?.lat,
                longitude = trackData.locations.lastOrNull()?.location?.long,
                locationTimestamp = trackData.locations.lastOrNull()?.timestamp?.inWholeMilliseconds?.formatMillisToDateString(),
                locationTimestampRaw = trackData.locations.lastOrNull()?.timestamp?.inWholeMilliseconds,
                temperatureCelsius = trackData.temperature?.temperatureCelsius,
                temperatureTimestamp = trackData.temperature?.timestampMillis?.formatMillisToDateString(),
                temperatureTimestampRaw = trackData.temperature?.timestampMillis,
                exported = false,
                networkType = trackData.networkInfo?.type.toString(),
                networkInfoTimestamp = trackData.networkInfo?.timestampMillis?.formatMillisToDateString(),
                networkInfoTimestampRaw = trackData.networkInfo?.timestampMillis,
                mobileNetworkOperator = mobileNetworkInfo?.operatorName,
                mobileNetworkType = mobileNetworkInfo?.networkType.toString(),
                signalStrength = mobileNetworkInfo?.primarySignalDbm,
                connectionStatus = if (trackData.internetConnectionConnected) "CONNECTED" else "DISCONNECTED",
                cellBand = mobileNetworkInfo?.primaryCell?.band?.band.toString(),
                cellBandFrequencyDownload = mobileNetworkInfo?.primaryCell?.band?.frequencyDL.toString(),
                cellBandName = mobileNetworkInfo?.primaryCell?.band?.name,
                cellBandNameInformal = mobileNetworkInfo?.primaryCell?.band?.informalName,
                appVersion = packageInfoProvider.versionName,
                id = null,
            )
            trackRepository.upsertTrack(track)
        }
    }

    fun Long.formatMillisToDateString(): String {
        val instant = Instant.ofEpochMilli(this)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return dateTime.format(formatter)
    }
}
