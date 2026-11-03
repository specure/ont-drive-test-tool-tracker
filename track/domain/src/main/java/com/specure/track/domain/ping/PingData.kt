package com.specure.track.domain.ping

import java.net.InetAddress


data class PingResult(
    val sequenceNumber: Int?,
    val packetSize: Int?,
    val ms: Long?,
    val timestamp: Long
)

data class PingStats(
    val ip: InetAddress,
    val packetsTransmitted: Int,
    val packetsReceived: Int,
    val packetLoss: Float,
    val stats: LatencyStats?,
    val result: PingResult
)

data class LatencyStats(
    val minMs: Long,
    val avgMs: Long,
    val maxMs: Long
)