package com.cadrikmdev.core.domain.location

import kotlin.time.Duration

data class LocationWithDetails(
    val location: Location,
    val source: String,
    val altitude: Double,
    val age: Duration,
    val timestamp: Duration,
)
