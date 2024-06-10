package com.cadrikmdev.iperf.domain

abstract class IperfOutputParser {

    abstract fun parseOutput(output: String)

    abstract fun parseIperfVersion(version: String)

    abstract fun parseClientInformation(clientInfo: String)

    abstract fun parseStartTime(startTime: String)

    abstract fun parseConnectingToHostInfo(hostInfo: String)

    abstract fun parseMode(hostMode: String)

    abstract fun parseConnectionInfo(connectionInfo: String)

    abstract fun parseStartingTestInfo(startingTestInfo: String)

    abstract fun parseTestProgressHeader(testProgressHeader: String)

    abstract fun parseTestProgress(testProgress: String)

    abstract fun parseTestingPhaseEnd(testingPhaseEnd: String)

    abstract fun parseEndHeader(testingPhaseEnd: String)

    abstract fun parseEnd(error: String)

    abstract fun parseEndCpuUtil(cpuUtilization: String)
}