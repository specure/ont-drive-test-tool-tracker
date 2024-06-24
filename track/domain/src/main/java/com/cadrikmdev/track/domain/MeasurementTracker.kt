package com.cadrikmdev.track.domain

import com.cadrikmdev.connectivity.domain.NetworkTracker
import com.cadrikmdev.core.domain.Timer
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.core.domain.track.TemperatureInfoObserver
import com.cadrikmdev.iperf.domain.IperfOutputParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
import kotlin.time.Duration

class MeasurementTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
    private val temperatureInfoReceiver: TemperatureInfoObserver,
    private val mobileNetworkTracker: NetworkTracker,
    private val iperfParser: IperfOutputParser,
) {
    private val _trackData = MutableStateFlow(TrackData())
    val trackData = _trackData.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    val currentLocation = isObservingLocation
        .flatMapLatest { isObservingLocation ->
            if (isObservingLocation) {
                locationObserver.observeLocation(1000L)
            } else flowOf()
        }
        .stateIn(
            applicationScope,
            SharingStarted.Lazily,
            null
        )

    init {

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
            temperatureInfoReceiver.register()
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
                    val newList = buildList {
                        addAll(trackData.value.locations)
                    }.toList()
                    _trackData.update {
                        it.copy(
                            locations = newList
                        )
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
            }
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
                val newLocationsList = trackData.value.locations

                val currentDuration = locationWithDetails.durationTimestamp


                _trackData.update {
                        it.copy(
                            locations = newLocationsList
                        )
                }
            }
            .launchIn(applicationScope)
    }


    fun setIsTracking(isTracking: Boolean) {
        this._isTracking.value = isTracking
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
    }

    fun finishTrack() {
        stopObservingLocation()
        setIsTracking(false)
        _elapsedTime.value = Duration.ZERO
        _trackData.value = TrackData()
    }
}
