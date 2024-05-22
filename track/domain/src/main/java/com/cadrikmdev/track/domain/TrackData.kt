package com.cadrikmdev.track.domain

import com.cadrikmdev.core.domain.location.LocationTimestamp
import kotlin.time.Duration

data class TrackData(
    val distanceMeters: Int = 0,
    val pace: Duration = Duration.ZERO,
    val locations: List<List<LocationTimestamp>> = emptyList()
)
