package com.cadrikmdev.run.domain

import com.cadrikmdev.core.domain.Timer
import com.cadrikmdev.core.domain.locaiton.LocationTimestamp
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
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RunningTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope,
) {

    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()

    private val isTracking = MutableStateFlow(false)
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
        isTracking
            .onEach { isTracking ->
                if (!isTracking) {
                    val newList = buildList {
                        addAll(runData.value.locations)
                        add(emptyList<LocationTimestamp>())
                    }.toList()
                    _runData.update {
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
            .combineTransform(isTracking) { location, isTracking ->
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
            .onEach { locationWithTimestamp ->
                val currentLocations = runData.value.locations
                val lastLocationsList = if (currentLocations.isNotEmpty()) {
                    currentLocations.last() + locationWithTimestamp
                } else listOf(locationWithTimestamp)
                val newLocationsList = currentLocations.replaceLast(lastLocationsList)

                val distanceMeters = LocationDataCalculator.getTotalDistanceInMeters(
                    locations = newLocationsList
                )
                val distanceKm = distanceMeters / 1000.0
                val currentDuration = locationWithTimestamp.durationTimestamp

                val avgSecondsPerKm = if (distanceKm == 0.0) {
                    0
                } else {
                    (currentDuration.inWholeSeconds / distanceKm).roundToInt()
                }

                _runData.update {
                    RunData(
                        distanceMeters = distanceMeters,
                        pace = avgSecondsPerKm.seconds,
                        locations = newLocationsList
                    )
                }
            }
            .launchIn(applicationScope)
    }


    fun setIsTracking(isTracking: Boolean) {
        this.isTracking.value = isTracking
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
    }
}

private fun <E> List<List<E>>.replaceLast(replacement: List<E>): List<List<E>> {
    if (this.isEmpty()) {
        return listOf(replacement)
    } else {
        return this.dropLast(1) + listOf(replacement)
    }
}
