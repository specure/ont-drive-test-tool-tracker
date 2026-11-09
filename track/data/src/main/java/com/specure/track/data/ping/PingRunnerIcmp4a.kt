package com.specure.track.data.ping

import com.marsounjan.icmp4a.Icmp
import com.marsounjan.icmp4a.Icmp4a
import com.specure.track.domain.ping.PingRunner
import com.specure.track.domain.ping.PingRunnerState
import com.specure.track.domain.ping.PingRunnerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class PingRunnerIcmp4a(

) : PingRunner {

    val pingExecutor = Icmp4a()

    private val _pingRunnerState = MutableStateFlow<PingRunnerState>(PingRunnerState())
    override val pingState: StateFlow<PingRunnerState> = _pingRunnerState.asStateFlow()

    private var pingRunnerJob: Job? = null

    override suspend fun sendPing(host: String, timeoutMs: Long): StateFlow<PingRunnerState> {
        resetState()
        safePingExecution(host) {
            val status = pingExecutor.ping(
                host = host,
                timeoutMillis = timeoutMs
            )
            handlePingStatus(host, status)
        }
        return pingState
    }

    override suspend fun startPing(host: String, timeoutMs: Long, coroutineScope: CoroutineScope): StateFlow<PingRunnerState> {
        resetState()

        // For continuous pinging, exceptions happen during asynchronous flow collection,
        // so we use .catch {} instead of trying to wrap launchIn in a standard try-catch.
        pingRunnerJob = pingExecutor.pingInterval(
            host = host,
            count = null,
            timeoutMillis = timeoutMs,
            intervalMillis = 1000
        )
            .onEach { status ->
                handlePingStatus(host, status)
                currentCoroutineContext().ensureActive()
            }
            .catch { error ->
                handleException(host, error)
            }
            .launchIn(coroutineScope)

        return pingState
    }

    override fun stopPing(): PingRunnerState {
        pingRunnerJob?.cancel()
        pingRunnerJob = null
        val lastState = _pingRunnerState.value
        return lastState
    }

    fun resetState() {
        _pingRunnerState.value = PingRunnerState()
    }

    private fun handlePingStatus(host: String, status: Icmp.PingStatus) {
        val result = status.result
        when (result) {
            is Icmp.PingResult.Success -> {
                Timber.d("$host(${status.ip.hostAddress}) ${result.packetSize} bytes - ${result.ms} ms")
                updateState(status, PingRunnerStatus.Running)
            }

            is Icmp.PingResult.Failed -> {
                Timber.d("$host(${status.ip.hostAddress}) Failed: ${result.message}")
                updateState(status, PingRunnerStatus.Error(Exception(result.message)))
            }
        }
    }

    private fun updateState(status: Icmp.PingStatus, runnerStatus: PingRunnerStatus) {
        val result = status.result
        when (result) {
            is Icmp.PingResult.Success -> {
                _pingRunnerState.value = _pingRunnerState.value.copy(
                    pingStats = status.toPingStats(),
                    pingRunnerStatus = runnerStatus
                )
            }

            is Icmp.PingResult.Failed -> {
                _pingRunnerState.value = _pingRunnerState.value.copy(
                    pingStats = status.toPingStats(),
                    pingRunnerStatus = runnerStatus
                )
            }
        }
    }

    /**
     * Unified error handler for both regular execution and asynchronous flow collection.
     */
    private fun handleException(host: String, error: Throwable) {
        when (error) {
            is Icmp.Error.UnknownHost -> Timber.d("Unknown host $host")
            is Icmp.Error.SocketException -> Timber.d("Socket Exception $host")
            is Icmp.Error.ProtocolException -> Timber.d("Protocol Exception $host")
            else -> Timber.d(error, "Unknown ICMP error for $host")
        }
        _pingRunnerState.value = _pingRunnerState.value.copy(
            pingRunnerStatus = PingRunnerStatus.Error(Exception(error.message))
        )
    }

    /**
     * Unified try-catch mechanism for regular suspending functions (like sendPing).
     */
    private suspend inline fun safePingExecution(host: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (error: Throwable) {
            handleException(host, error)
        }
    }
}

