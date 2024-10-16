package com.specure.iperf.domain

data class IperfError(
    val timestamp: Long,
    val error: String
)