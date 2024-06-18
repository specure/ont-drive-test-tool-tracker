package com.cadrikmdev.track.presentation.active_track.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.core.domain.location.LocationWithDetails
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline

@Composable
fun SignalTrackerPolylines(locations: List<LocationWithDetails>) {
    val polyline = remember(locations) {

            locations.zipWithNext { timestamp1, timestamp2 ->
                PolylineUi(
                    location1 = timestamp1.location,
                    location2 = timestamp2.location,
                    color = PolylineColorCalculator.locationsToColor(
                        location1 = timestamp1,
                        location2 = timestamp2,
                    )
                )
            }

    }

    polyline.forEach { polylineUi ->
        Polyline(
            points = listOf(
                LatLng(polylineUi.location1.lat, polylineUi.location1.long),
                LatLng(polylineUi.location2.lat, polylineUi.location2.long),
            ),
            color = polylineUi.color,
            jointType = JointType.BEVEL
        )
    }
}