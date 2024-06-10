package com.cadrikmdev.iperf.domain

data class IperfTest(
    val version: String?,
    val startTimestampRaw: String?,
    val startTimestamp: Long?,
    val iperfServerInfo: IperfServerInfo?,
    val connectionInfo: IperfConnectionInfo?,
    val testProgress: List<IperfTestProgress>?,
    val error: String?
)
