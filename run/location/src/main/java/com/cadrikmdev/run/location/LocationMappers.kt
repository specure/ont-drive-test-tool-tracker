package com.cadrikmdev.run.location

import android.location.Location
import com.cadrikmdev.core.domain.locaiton.LocationWithAltitude


fun Location.toLocationWithAltitude(): LocationWithAltitude {
    return LocationWithAltitude(
        location = com.cadrikmdev.core.domain.locaiton.Location(
            lat = latitude,
            long = longitude
        ),
        altitude = altitude
    )

}