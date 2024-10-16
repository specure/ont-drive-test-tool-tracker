package com.specure.iperf.domain

data class IperfTestProgressUpload(
    val retransmissions: Int,
    val congestionWindow: Int,
    val congestionWindowUnit: String,
    override val timestampMillis: Long?,
    override val relativeTestStartIntervalStart: Double?,
    override val relativeTestStartIntervalEnd: Double?,
    override val relativeTestStartIntervalUnit: String?,
    override val transferred: Double?,
    override val transferredUnit: String?,
    override val bandwidth: Double?,
    override val bandwidthUnit: String?
) : IperfTestProgress