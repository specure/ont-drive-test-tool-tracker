package com.specure.track.domain

import com.specure.connectivity.domain.NetworkInfo
import com.specure.core.domain.Temperature
import com.specure.core.domain.location.LocationWithDetails
import com.specure.iperf.domain.IperfTest
import com.specure.iperf.domain.IperfTestStatus
import kotlin.time.Duration

data class TrackData(
    val startTime: Long = 0,
    val duration: Duration = Duration.ZERO,
    val locations: List<LocationWithDetails> = emptyList(),
    val temperature: Temperature? = null,
    val networkInfo: NetworkInfo? = null,
    val iperfTestUpload: IperfTest? = null,
    val iperfTestDownload: IperfTest? = null,
    val internetConnectionConnected: Boolean = false,
    val isSpeedTestEnabled: Boolean = false,
) {
    fun isSpeedTestError(): Boolean {
        return isDownloadSpeedTestError() || isUploadSpeedTestError()
    }

    fun isDownloadSpeedTestError(): Boolean {
        return (isSpeedTestEnabled && (iperfTestDownload?.error?.isNotEmpty() == true || iperfTestDownload?.status == IperfTestStatus.ERROR))
    }

    fun isUploadSpeedTestError(): Boolean {
        return (isSpeedTestEnabled && (iperfTestUpload?.error?.isNotEmpty() == true || iperfTestUpload?.status == IperfTestStatus.ERROR))
    }

    fun getDownloadSpeedInMbitsPerSec(): Double? {
        val speed =
            if (isSpeedTestEnabled) iperfTestDownload?.testProgress?.lastOrNull()?.bandwidth else null
        val unit =
            if (isSpeedTestEnabled) iperfTestDownload?.testProgress?.lastOrNull()?.bandwidthUnit else null

        if (speed == null || unit == null) {
            return null
        }
        return transformToMbitsPerSec(unit, speed)
    }

    fun getUploadSpeedInMbitsPerSec(): Double? {
        val speed =
            if (isSpeedTestEnabled) iperfTestUpload?.testProgress?.lastOrNull()?.bandwidth else null
        val unit =
            if (isSpeedTestEnabled) iperfTestUpload?.testProgress?.lastOrNull()?.bandwidthUnit else null

        if (speed == null || unit == null) {
            return null
        }
        return transformToMbitsPerSec(unit, speed)
    }

    private fun transformToMbitsPerSec(unit: String, speed: Double) = when (unit) {
        "bits/sec" -> speed / 1_000_000f
        "Kbits/sec" -> speed / 1_000f
        "Mbits/sec" -> speed
        "Gbits/sec" -> speed * 1_000f
        else -> null
    }
}
