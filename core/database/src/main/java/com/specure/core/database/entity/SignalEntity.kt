package com.specure.core.database.entity

import androidx.room.Entity
import com.specure.core.database.Tables

@Entity(
    tableName = Tables.SIGNAL,
    primaryKeys = ["timeNanos", "cellUuid"],
)
data class SignalEntity(
    val testUUID: String?,
    val signalChunkId: String?,
    val cellUuid: String,
    /**
     * difference between this update of the signal during the test and start time of the test
     */
    val timeNanos: Long,
    /**
     * difference between last update of the signal during the test and start time of the test
     */
    val timeNanosLast: Long?,

    val transportType: Int,

    val mobileNetworkType: Int?,

    /**
     * NR connection state from netmonster magic during the signal obtaining, added because of 5G NSA (we have inactive NR cells found with signal
     * information, but it is still NSA mode, so we want to distinguish it somehow - problem is we do not know how to report pure 5G then, we will
     * need to debug it when 5G SA will be available in some country)
     */
    val nrConnectionState: String,

    // wifi
    val signal: Int?,
    val wifiLinkSpeed: Int?,
    // 2G/3G
    val bitErrorRate: Int?,
    // 4G
    val lteRsrp: Int?,
    val lteRsrq: Int?,
    val lteRssnr: Int?,
    val lteCqi: Int?,
    val timingAdvance: Int?,
    // 5G
    val nrCsiRsrp: Int?,
    val nrCsiRsrq: Int?,
    val nrCsiSinr: Int?,
    val nrSsRsrp: Int?,
    val nrSsRsrq: Int?,
    val nrSsSinr: Int?
) {
    fun hasNonNullSignal(): Boolean {
        return listOfNotNull(
            signal,
            wifiLinkSpeed,
            bitErrorRate,
            lteRsrp,
            lteRsrq,
            lteRssnr,
            lteCqi,
            nrCsiRsrp,
            nrCsiRsrq,
            nrCsiSinr,
            nrSsRsrp,
            nrSsRsrq,
            nrSsSinr
        ).isNotEmpty()
    }
}