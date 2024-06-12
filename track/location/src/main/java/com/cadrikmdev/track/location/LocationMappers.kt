package com.cadrikmdev.track.location

import android.location.Location
import android.os.SystemClock
import com.cadrikmdev.core.domain.location.LocationWithDetails
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun Location.toLocationWithDetails(): LocationWithDetails {
    return LocationWithDetails(
        location = com.cadrikmdev.core.domain.location.Location(
            lat = latitude,
            long = longitude
        ),
        altitude = altitude,
        source = provider ?: "UNKNOWN",
        age = elapsedRealtimeNanos.toDuration(DurationUnit.NANOSECONDS),
        timestamp = System.currentTimeMillis().toDuration(DurationUnit.MILLISECONDS)
    )
}