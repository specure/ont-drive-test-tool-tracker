package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.connectivity.domain.mobile.MobileNetworkInfo
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

data class TrackOverviewState(
    val tracks: List<TrackUi> = emptyList(),
    val isOnline: Boolean = false,
    val isLocationTrackable: Boolean = false,
    val isPermissionRequired: Boolean = false,
    val isWifiServiceEnabled: Boolean = false,
    val isLocationServiceEnabled: Boolean = false,
    val isLocationServiceResolvable: Boolean = false,
    val mobileNetworkInfo: MobileNetworkInfo? = null,
    val currentIperfDownloadSpeed: String? = null,
    val currentIperfDownloadSpeedUnit: String? = null,
    val currentIperfUploadSpeed: String? = null,
    val currentIperfUploadSpeedUnit: String? = null,
    val currentTemperatureCelsius: Temperature? = null,
    val isIperfUploadRunning: Boolean = false,
    val isIperfDownloadRunning: Boolean = false,
    val currentIperfDownloadInfoRaw: String? = null,
    val currentIperfUploadInfoRaw: String? = null,
    val location: LocationTimestamp? = null,
)
