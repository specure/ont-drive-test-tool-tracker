package com.specure.iperf.domain

interface IperfTestProgress {
    val timestampMillis: Long?
    val relativeTestStartIntervalStart: Double?
    val relativeTestStartIntervalEnd: Double?
    val relativeTestStartIntervalUnit: String?
    val transferred: Double?
    val transferredUnit: String?
    val bandwidth: Double?
    val bandwidthUnit: String?
}
