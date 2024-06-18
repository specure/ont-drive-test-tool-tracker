package com.cadrikmdev.iperf.domain

data class IperfTest(
    val version: String? = null,
    val startTimestampRaw: String? = null,
    val startTimestamp: Long? = null,
    val iperfServerInfo: IperfServerInfo? = null,
    val connectionInfo: IperfConnectionInfo? = null,
    val testProgress: List<IperfTestProgress>? = null,
    val error: List<IperfError?>? = null,
    val rawUnparsedLog: String? = null
)
