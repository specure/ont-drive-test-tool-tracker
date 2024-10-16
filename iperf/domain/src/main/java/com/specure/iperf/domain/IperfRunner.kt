package com.specure.iperf.domain

import kotlinx.coroutines.flow.StateFlow

interface IperfRunner {

    val testProgressDetailsFlow: StateFlow<IperfTest>

    fun startTest()

    fun stopTest()

    val zeroDownloadSpeedProgress: IperfTestProgress
        get() = IperfTestProgressDownload(
            timestampMillis = System.currentTimeMillis(),
            relativeTestStartIntervalStart = 0.0,
            relativeTestStartIntervalUnit = "sec",
            relativeTestStartIntervalEnd = 0.0,
            transferred = 0.0,
            transferredUnit = "bytes",
            bandwidth = 0.0,
            bandwidthUnit = "bits/sec",
        )

    val zeroUploadSpeedProgress: IperfTestProgress
        get() = IperfTestProgressUpload(
            timestampMillis = System.currentTimeMillis(),
            relativeTestStartIntervalStart = 0.0,
            relativeTestStartIntervalUnit = "sec",
            relativeTestStartIntervalEnd = 0.0,
            transferred = 0.0,
            transferredUnit = "bytes",
            bandwidth = 0.0,
            bandwidthUnit = "bits/sec",
            retransmissions = 0,
            congestionWindow = 0,
            congestionWindowUnit = "bytes"
        )
}