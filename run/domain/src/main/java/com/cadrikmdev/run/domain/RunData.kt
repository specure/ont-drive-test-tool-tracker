package com.cadrikmdev.run.domain

import com.cadrikmdev.core.domain.locaiton.LocationTimestamp
import kotlin.time.Duration

data class RunData(
    val distanceMeters: Int = 0,
    val pace: Duration = Duration.ZERO,
    val locations: List<List<LocationTimestamp>> = emptyList()
)
