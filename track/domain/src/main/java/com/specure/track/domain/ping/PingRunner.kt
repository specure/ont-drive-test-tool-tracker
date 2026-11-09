package com.specure.track.domain.ping

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface PingRunner {

    suspend fun sendPing(host: String = "8.8.8.8", timeoutMs: Long = 1000): StateFlow<PingRunnerState>

    suspend fun startPing(host: String = "8.8.8.8", timeoutMs: Long = 1000, coroutineScope: CoroutineScope): StateFlow<PingRunnerState>

    fun stopPing(): PingRunnerState
}