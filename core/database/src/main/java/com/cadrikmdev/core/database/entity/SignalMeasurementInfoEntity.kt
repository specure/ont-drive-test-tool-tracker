package com.cadrikmdev.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cadrikmdev.core.database.Tables

@Entity(tableName = Tables.SIGNAL_MEASUREMENT_INFO)
data class SignalMeasurementInfoEntity(

    /**
     * Measurement id from corresponding [SignalMeasurementEntity]
     */
    @PrimaryKey
    val measurementId: String,

    /**
     * Signal measurement UUID. Related only for one network in the loop. Another [SignalMeasurementEntity] must be used for another network.
     * Must be filled with data from server.
     */
    val uuid: String,

    /**
     * Client remote ip
     * Must be filled with data from server.
     */
    val clientRemoteIp: String,

    /**
     * Result Url
     * Must be filled with data from server.
     */
    val resultUrl: String,

    /**
     * Provider name
     */
    var provider: String?
)