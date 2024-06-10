package com.cadrikmdev.iperf.domain

import java.sql.Timestamp

data class IperfTestProgress(
    val timestampMillis: Long?,
    val relativeTestStartIntervalStart: Long?,
    val relativeTestStartIntervalEnd: Long?,
    val relativeTestStartIntervalUnit: String?,
    val transferred: Double?,
    val transferredUnit: String?,
    val bandwidth: Double?,
    val bandwidthUnit: String?,
)
