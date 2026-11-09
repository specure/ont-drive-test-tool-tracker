package com.specure.track.presentation.util.ping

sealed interface PingAction {
    data class OnPingTestStartClick(val host: String, val timeoutMs: Long) : PingAction
    data object OnPingTestStopClick : PingAction
}