package com.specure.track.data.ping

import com.marsounjan.icmp4a.Icmp4a
import com.specure.track.domain.ping.PingDataSource
import com.specure.track.domain.ping.PingRunnerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PingIcmp4aDataSource(
    private val pingExecutor: Icmp4a = Icmp4a()
) : PingDataSource {

    override fun observePing(host: String, timeoutMillis: Long, intervalMillis: Long): Flow<PingRunnerState> {
        return pingExecutor.pingInterval(
            host = host,
            count = null,
            timeoutMillis = timeoutMillis,
            intervalMillis = intervalMillis
        )
            .catch { error ->
                // propagate critical socketu/streamu errors to the caller
                throw error
            }
            .map { icmpStatus ->
                PingRunnerState(
                    pingStats = icmpStatus.toPingStats(),
                    pingRunnerStatus = icmpStatus.toPingRunnerStatus()
                )
            }
    }
}
