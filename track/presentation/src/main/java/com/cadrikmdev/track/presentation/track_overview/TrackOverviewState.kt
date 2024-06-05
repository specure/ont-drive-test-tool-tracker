package com.cadrikmdev.track.presentation.track_overview

import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkInfo
import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

data class TrackOverviewState(
    val tracks: List<TrackUi> = emptyList(),
    val isOnline: Boolean = false,
    val isPermissionRequired: Boolean = false,
    val isLocationServiceEnabled: Boolean = false,
    val isLocationServiceResolvable: Boolean = false,
    val mobileNetworkInfo: MobileNetworkInfo? = null,
    val currentIperfInfo: String? = null,
)
