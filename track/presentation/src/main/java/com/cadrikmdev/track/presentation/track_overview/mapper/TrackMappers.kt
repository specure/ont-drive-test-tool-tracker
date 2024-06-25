package com.cadrikmdev.track.presentation.track_overview.mapper

import com.cadrikmdev.core.domain.track.Track
import com.cadrikmdev.track.presentation.track_overview.model.TrackUi

fun Track.toTrackUi(): TrackUi {
    return TrackUi(
        id = id,
        durationMillis = durationMillis,
        timestamp = timestamp,
        timestampRaw = timestampRaw,
        downloadSpeed = downloadSpeed,
        downloadSpeedUnit = downloadSpeedUnit,
        downloadSpeedTestState = downloadSpeedTestState,
        downloadSpeedTestError = downloadSpeedTestError,
        downloadSpeedTestTimestamp = downloadSpeedTestTimestamp,
        downloadSpeedTestTimestampRaw = downloadSpeedTestTimestampRaw,
        uploadSpeed = uploadSpeed,
        uploadSpeedUnit = uploadSpeedUnit,
        uploadSpeedTestState = uploadSpeedTestState,
        uploadSpeedTestError = uploadSpeedTestError,
        uploadSpeedTestTimestamp = uploadSpeedTestTimestamp,
        uploadSpeedTestTimestampRaw = uploadSpeedTestTimestampRaw,
        latitude = latitude,
        longitude = longitude,
        locationTimestamp = locationTimestamp,
        locationTimestampRaw = locationTimestampRaw,
        networkType = networkType,
        mobileNetworkOperator = mobileNetworkOperator,
        mobileNetworkType = mobileNetworkType,
        signalStrength = signalStrength,
        networkInfoTimestamp = networkInfoTimestamp,
        networkInfoTimestampRaw = networkInfoTimestampRaw,
        connectionStatus = connectionStatus,
        temperatureCelsius = temperatureCelsius,
        temperatureTimestamp = temperatureTimestamp,
        temperatureTimestampRaw = temperatureTimestampRaw,
        exported = exported,
    )

}