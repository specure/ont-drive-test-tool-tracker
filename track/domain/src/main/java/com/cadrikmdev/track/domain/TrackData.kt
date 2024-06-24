package com.cadrikmdev.track.domain

import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.LocationWithDetails
import com.cadrikmdev.iperf.domain.IperfTestProgressDownload
import com.cadrikmdev.iperf.domain.IperfTestProgressUpload
import kotlin.time.Duration

data class TrackData(
    val startTime: Long = 0,
    val duration: Duration = Duration.ZERO,
    val locations: List<LocationWithDetails> = emptyList(),
    val downloadProgress: IperfTestProgressDownload? = null,
    val uploadProgress: IperfTestProgressUpload? = null,
    val temperature: Temperature? = null,
    val connected: Boolean = false,
)
