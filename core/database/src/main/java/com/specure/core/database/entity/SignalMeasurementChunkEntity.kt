package com.specure.core.database.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.specure.core.database.Columns
import com.specure.core.database.Tables
import java.util.UUID

@Keep
@Entity(tableName = Tables.SIGNAL_MEASUREMENT_CHUNK)
data class SignalMeasurementChunkEntity(

    @PrimaryKey
    @ColumnInfo(name = Columns.SIGNAL_MEASUREMENT_ID_PARENT_COLUMN)
    val id: String = UUID.randomUUID().toString(),

    val measurementId: String,

    val sequenceNumber: Int,

    /**
     * Phase which was the last done signal measurement loop
     */
    var state: String,

    /**
     * Stacktrace of IllegalNetworkChangeException exception that occurred during signal measurement
     * May be null if test was success or cancelled
     */
    var testErrorCause: String? = null,

    /**
     * Current time_ns of the client at the time of this submission. In case of resubmission, this time changes. This property is
     * only mandatory on sequence_number:0 time_ns:0 is defined as the moment that
     * signal measurement is initiated, either by the user or by network change.
     */
    var startTimeNanos: Long,

    var submissionRetryCount: Int
)