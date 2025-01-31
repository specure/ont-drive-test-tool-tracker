package com.specure.track.presentation.track_overview.model

import com.specure.core.domain.track.TrackId

data class TrackUi(
    val id: TrackId, // null if new track
    val durationMillis: Long,
    val timestamp: String,
    val timestampRaw: Long,
    val downloadSpeed: Double?,
    val downloadSpeedUnit: String?,
    val downloadSpeedTestState: String?,
    val downloadSpeedTestError: String?,
    val downloadSpeedTestTimestamp: String?,
    val downloadSpeedTestTimestampRaw: Long?,
    val uploadSpeed: Double?,
    val uploadSpeedUnit: String?,
    val uploadSpeedTestState: String?,
    val uploadSpeedTestError: String?,
    val uploadSpeedTestTimestamp: String?,
    val uploadSpeedTestTimestampRaw: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val locationTimestamp: String?,
    val locationTimestampRaw: Long?,
    val networkType: String?,
    val mobileNetworkOperator: String?,
    val mobileNetworkType: String?,
    val signalStrength: Int?,
    val networkInfoTimestamp: String?,
    val networkInfoTimestampRaw: Long?,
    val connectionStatus: String?,
    val temperatureCelsius: Double?,
    val temperatureTimestamp: String?,
    val temperatureTimestampRaw: Long?,
    val exported: Boolean = false,
)
