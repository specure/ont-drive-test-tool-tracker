package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables

@Entity(
    tableName = Tables.TRACK_ENTITY
)
data class TrackEntity(
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
    @PrimaryKey(autoGenerate = true)
    val id: Long?
)
