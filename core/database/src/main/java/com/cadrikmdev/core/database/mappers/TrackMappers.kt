package com.cadrikmdev.core.database.mappers

import com.cadrikmdev.core.database.entity.TrackEntity
import com.cadrikmdev.core.domain.location.Location
import com.cadrikmdev.core.domain.track.Track
import org.bson.types.ObjectId
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun TrackEntity.toTrack(): Track {
    return Track(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUtc = Instant.parse(dateTimeUtc)
            .atZone(ZoneId.of("UTC")),
        distanceMeters = distanceMeters,
        location = Location(
            lat = latitude,
            long = longitude
        ),
        maxSpeedKmh = maxSpeedKmh,
        totalElevationMeters = totalElevationMeters,
    )
}

fun Track.toTrackEntity(): TrackEntity {
    return TrackEntity(
        id = id ?: ObjectId().toHexString(),
        durationMillis = duration.inWholeMilliseconds,
        dateTimeUtc = dateTimeUtc.toInstant().toString(),
        distanceMeters = distanceMeters,
        latitude = location.lat,
        longitude = location.long,
        maxSpeedKmh = maxSpeedKmh,
        avgSpeedKmh = avgSpeedKmh,
        totalElevationMeters = totalElevationMeters,
    )
}