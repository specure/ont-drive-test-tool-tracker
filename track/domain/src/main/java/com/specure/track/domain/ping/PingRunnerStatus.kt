package com.specure.track.domain.ping


sealed interface PingRunnerStatus {
    data object Idle : PingRunnerStatus
    data object Running : PingRunnerStatus
    data class Error(val exception: Exception) : PingRunnerStatus
}

fun PingRunnerStatus.toHumanReadableString(): String {
    return when (this) {
        PingRunnerStatus.Idle -> "Idle"
        PingRunnerStatus.Running -> "Running"
        is PingRunnerStatus.Error -> "Error: ${exception.message}"
    }
}