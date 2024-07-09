package com.cadrikmdev.track.domain

import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.LocationWithDetails
import com.cadrikmdev.iperf.domain.IperfTest
import kotlin.time.Duration

data class TrackData(
    val startTime: Long = 0,
    val duration: Duration = Duration.ZERO,
    val locations: List<LocationWithDetails> = emptyList(),
    val temperature: Temperature? = null,
    val networkInfo: NetworkInfo? = null,
    val connected: Boolean = false,
    val iperfTestUpload: IperfTest? = null,
    val iperfTestDownload: IperfTest? = null,
    val internetConnectionConnected: Boolean = false,
    val isSpeedTestEnabled: Boolean = false,
)
