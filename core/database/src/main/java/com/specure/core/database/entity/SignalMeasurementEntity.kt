package com.specure.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.specure.core.database.Tables
import java.util.UUID

@Entity(tableName = Tables.SIGNAL_MEASUREMENT)
data class SignalMeasurementEntity(

    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Unique cell UUID
     */
    val networkUUID: String,

    /**
     * Test start time in millis
     */
    val startTimeMillis: Long = System.currentTimeMillis(),

    /**
     * Test start time in nanos
     */
    val startTimeNanos: Long = System.nanoTime(),

    /**
     * Type of mobile network
     */
    var mobileNetworkType: Int? = null,

    /**
     * Type of the network
     */
    var transportType: Int?,

    /**
     * network capabilies obtained from android api - not modified
     */
    var rawCapabilitiesRecord: String?,
)