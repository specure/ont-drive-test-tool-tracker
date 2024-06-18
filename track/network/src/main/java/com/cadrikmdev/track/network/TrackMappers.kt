package com.cadrikmdev.track.network

import com.cadrikmdev.core.domain.location.Location
import com.cadrikmdev.core.domain.track.Track
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun TrackDto.toTrack(): Track {
    return Track(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUtc = Instant.parse(dateTimeUtc)
            .atZone(ZoneId.of("UTC")),
        location = Location(lat, long),
    )
}

fun Track.toCreateTrackRequest(): CreateTrackRequest {
    return CreateTrackRequest(
        id = id!!,
        epochMillis = dateTimeUtc.toEpochSecond() * 1000L,
        durationMillis = duration.inWholeMilliseconds,
        lat = location.lat,
        long = location.long,
    )
}
