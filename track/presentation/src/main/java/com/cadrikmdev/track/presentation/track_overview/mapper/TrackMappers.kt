package com.cadrikmdev.track.presentation.track_overview.mapper

import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.core.presentation.ui.formatted
import com.cadrikmdev.core.presentation.ui.toFormattedKm
import com.cadrikmdev.core.presentation.ui.toFormattedKmh
import com.cadrikmdev.core.presentation.ui.toFormattedMeters
import com.cadrikmdev.core.presentation.ui.toFormattedPace
import com.cadrikmdev.track.presentation.track_overview.model.TrackUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Track.toTrackUi(): TrackUi {
    val dateTimeInLocalTime = dateTimeUtc
        .withZoneSameInstant(ZoneId.systemDefault())
    val formattedDateTime = DateTimeFormatter
        .ofPattern("MMM dd, yyyy - hh:mma")
        .format(dateTimeInLocalTime)

    val distanceKm = distanceMeters / 1000.0

    return TrackUi(
        id = id!!,
        duration = duration.formatted(),
        dateTime = formattedDateTime,
        distance = distanceKm.toFormattedKm(),
        avgSpeed = avgSpeedKmh.toFormattedKmh(),
        maxSpeed = maxSpeedKmh.toFormattedKmh(),
        totalElevation = totalElevationMeters.toFormattedMeters(),
        pace = duration.toFormattedPace(distanceKm),
    )

}