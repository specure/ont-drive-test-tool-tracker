package com.specure.track.presentation.util.ping

import com.specure.track.domain.ping.PingRunner
import com.specure.track.domain.ping.PingRunnerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PingManager(
    private val pingRunner: PingRunner
) {
    // The manager owns the state flow
    private val _pingState = MutableStateFlow<PingRunnerState>(PingRunnerState())
    val pingState: StateFlow<PingRunnerState> = _pingState.asStateFlow()

    suspend fun startPing(host: String, timeoutMs: Long, scope: CoroutineScope) {
        // Pass a callback or let the runner update the manager's state.
        // Alternatively, if PingRunner already has a state flow, you can collect it here.
        pingRunner.startPing(host, timeoutMs, scope).collect { newState ->
            _pingState.value = newState
        }
    }

    suspend fun sendSinglePing(host: String) {
        val newState = pingRunner.sendPing(host)
        // update state accordingly
    }

    fun stopPing() {
        pingRunner.stopPing()
        _pingState.value = PingRunnerState() // Reset state if needed
    }
}