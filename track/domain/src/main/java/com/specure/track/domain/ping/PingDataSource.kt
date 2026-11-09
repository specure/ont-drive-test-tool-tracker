package com.specure.track.domain.ping

import kotlinx.coroutines.flow.Flow

interface PingDataSource {
    fun observePing(host: String, timeoutMillis: Long = 1000, intervalMillis: Long = 1000): Flow<PingRunnerState>
}

class PingObserver(
    private val dataSource: PingDataSource
) {

    fun startPing(
        host: String,
        timeoutMillis: Long = 1000,
        intervalMillis: Long = 1000
    ): Flow<PingRunnerState> {
        return dataSource.observePing(host, timeoutMillis, intervalMillis)
    }
}
