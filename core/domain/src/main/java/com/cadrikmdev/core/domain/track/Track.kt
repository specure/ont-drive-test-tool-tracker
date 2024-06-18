package com.cadrikmdev.core.domain.track

import com.cadrikmdev.core.domain.location.Location
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.DurationUnit

data class Track(
    val id: String?, // null if new track
    val duration: Duration,
    val dateTimeUtc: ZonedDateTime,
    val location: Location,
)
