package com.specure.iperf.domain

data class IperfConnectionInfo(
    val localIp: String?,
    val localPort:String?,
    val hostIp: String?,
    val hostPort: String?,
)