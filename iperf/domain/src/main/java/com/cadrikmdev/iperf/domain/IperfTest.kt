package com.cadrikmdev.iperf.domain

data class IperfTest(
    val version: String? = null,
    val startTimestampRaw: String? = null,
    val startTimestamp: Long? = null,
    val iperfServerInfo: IperfServerInfo? = null,
    val connectionInfo: IperfConnectionInfo? = null,
    val testProgress: List<IperfTestProgress> = emptyList(),
    val error: List<IperfError> = emptyList(),
    val rawUnparsedLog: String? = null,
    val status: IperfTestStatus = IperfTestStatus.NOT_STARTED,
    val direction: IperfTestDirection = IperfTestDirection.UNDEFINED,
)

enum class IperfTestStatus {
    NOT_STARTED,
    INITIALIZING,
    RUNNING,
    ENDED,
    ERROR,
    STOPPED,
}

enum class IperfTestDirection {
    UNDEFINED,
    DOWNLOAD,
    UPLOAD,
}