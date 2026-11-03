package com.specure.track.domain.ping

data class PingRunnerState(
    val pingStats: PingStats? = null,
    val pingRunnerStatus: PingRunnerStatus = PingRunnerStatus.Idle
)