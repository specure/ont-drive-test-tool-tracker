package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.connectivity.domain.NetworkInfo
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.track.presentation.track_overview.model.FileExportUi

data class TrackOverviewState(
    val isOnline: Boolean = false,
    val isLocationTrackable: Boolean = false,
    val isPermissionRequired: Boolean = false,
    val isWifiServiceEnabled: Boolean = false,
    val isLocationServiceEnabled: Boolean = false,
    val isLocationServiceResolvable: Boolean = false,
    val mobileNetworkInfo: NetworkInfo? = null,
    val currentTemperatureCelsius: Temperature? = null,
    val location: LocationTimestamp? = null,
    val fileExport: FileExportUi? = null,
    val trackCountForExport: Int = 0,
    val isSpeedTestEnabled: Boolean = false,
) {
    fun isPossibleToStartMeasurement(): Boolean {
        return isLocationServiceEnabled && !isWifiServiceEnabled && isLocationTrackable && isLocationServiceResolvable && location != null && !isPermissionRequired
    }
}
