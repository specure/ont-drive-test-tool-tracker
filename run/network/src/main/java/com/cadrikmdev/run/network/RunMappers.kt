package com.cadrikmdev.run.network

import com.cadrikmdev.core.domain.locaiton.Location
import com.cadrikmdev.core.domain.run.Run
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

fun RunDto.toRun(): Run {
    return Run(
        id = id,
        duration = durationMillis.milliseconds,
        dateTimeUtc = Instant.parse(dateTimeUtc)
            .atZone(ZoneId.of("UTC")),
        distanceMeters = distanceMeters,
        location = Location(lat, long),
        maxSpeedKmh = maxSpeedKmh,
        totalElevationMeters = totalElevationMeters,
        mapPictureUrl = mapPictureUrl
    )
}

fun Run.toCreateRunRequest(): CreateRunRequest {
    return CreateRunRequest(
        id = id!!,
        epochMillis = dateTimeUtc.toEpochSecond() * 1000L,
        durationMillis = duration.inWholeMilliseconds,
        distanceMeters = distanceMeters,
        lat = location.lat,
        long = location.long,
        avgSpeedKmh = avgSpeedKmh,
        maxSpeedKmh = maxSpeedKmh,
        totalElevationMeters = totalElevationMeters
    )
}

//fun Run.toRunDto(): RunDto {
//    return RunDto(
//        id = id!!,
//        dateTimeUtc = dateTimeUtc.toInstant().toString(),
//        durationMillis = duration.inWholeMilliseconds,
//        distanceMeters = distanceMeters,
//        lat = location.lat,
//        long = location.long,
//        avgSpeedKmh = avgSpeedKmh,
//        maxSpeedKmh = maxSpeedKmh,
//        totalElevationMeters = totalElevationMeters,
//        mapPictureUrl = mapPictureUrl
//    )
//}