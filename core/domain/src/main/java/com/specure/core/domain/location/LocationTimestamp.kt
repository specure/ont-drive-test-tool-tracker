package com.specure.core.domain.location

import kotlin.time.Duration

data class LocationTimestamp(
    val location: LocationWithDetails,
    val durationTimestamp: Duration
)
