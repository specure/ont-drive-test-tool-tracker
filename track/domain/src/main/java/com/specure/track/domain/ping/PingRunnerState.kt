package com.specure.track.domain.ping

data class PingRunnerState(
    val pingStats: PingStats? = null,
    val pingRunnerStatus: PingRunnerStatus = PingRunnerStatus.Idle
) {
    fun isPingRunning(): Boolean {
        return when (pingRunnerStatus) {
            PingRunnerStatus.Idle -> false
            PingRunnerStatus.Running -> true
            is PingRunnerStatus.Error -> false
        }
    }
}
