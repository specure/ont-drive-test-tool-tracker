package com.cadrikmdev.track.location

import android.location.Location
import com.cadrikmdev.core.domain.location.LocationWithAltitude


fun Location.toLocationWithAltitude(): LocationWithAltitude {
    return LocationWithAltitude(
        location = com.cadrikmdev.core.domain.location.Location(
            lat = latitude,
            long = longitude
        ),
        altitude = altitude
    )

}