package com.specure.track.domain.ping


sealed interface PingRunnerStatus {
    data object Idle : PingRunnerStatus
    data object Running : PingRunnerStatus
    data class Error(val exception: Exception) : PingRunnerStatus
}