package com.specure.track.data.ping

import com.marsounjan.icmp4a.Icmp
import com.specure.track.domain.ping.LatencyStats
import com.specure.track.domain.ping.PingResult
import com.specure.track.domain.ping.PingRunnerStatus
import com.specure.track.domain.ping.PingStats

fun Icmp.PingResult.Success.toPingResult(): PingResult {
    return PingResult(
        sequenceNumber = this.sequenceNumber,
        packetSize = this.packetSize,
        ms = this.ms,
        timestamp = System.currentTimeMillis()
    )
}

fun Icmp.PingResult.Failed.toPingResult(): PingResult {
    return PingResult(
        sequenceNumber = null,
        packetSize = null,
        ms = null,
        timestamp = System.currentTimeMillis()
    )
}

fun Icmp.PingStatus.toPingResult(): PingResult {
    return when (val result = this.result) {
        is Icmp.PingResult.Success -> result.toPingResult()
        is Icmp.PingResult.Failed -> result.toPingResult()
    }
}

fun Icmp.LatencyStats.toLatencyStats(): LatencyStats {
    return LatencyStats(
        minMs = this.minMs,
        avgMs = this.avgMs,
        maxMs = this.maxMs
    )
}

fun Icmp.PingStatus.toPingStats(): PingStats {
    return PingStats(
        ip = this.ip,
        packetsTransmitted = this.packetsTransmitted,
        packetsReceived = this.packetsReceived,
        packetLoss = this.packetLoss,
        stats = this.stats?.toLatencyStats(),
        result = this.toPingResult()
    )
}

fun Icmp.PingStatus.toPingRunnerStatus(): PingRunnerStatus {
    return when (val result = this.result) {
        is Icmp.PingResult.Success -> PingRunnerStatus.Running
        is Icmp.PingResult.Failed -> PingRunnerStatus.Error(Exception(result.message))
    }
}
