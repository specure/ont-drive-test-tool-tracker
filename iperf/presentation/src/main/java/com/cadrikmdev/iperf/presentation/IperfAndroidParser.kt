package com.cadrikmdev.iperf.presentation

import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.iperf.domain.IperfTestProgress
import com.cadrikmdev.iperf.domain.IperfTestProgressDownload
import com.cadrikmdev.iperf.domain.IperfTestProgressUpload
import timber.log.Timber

class IperfAndroidParser: IperfOutputParser() {

    override fun parseOutput(output: String) {
        TODO("Not yet implemented")
    }

    override fun parseIperfVersion(version: String) : String? {
        if (version.startsWith("iperf")) {
            val versionSplit = version.split(" ")
            if (versionSplit.size > 1) {
                return versionSplit[1]
            }
        }
        return null
    }

    override fun parseClientInformation(clientInfo: String) {
        TODO("Not yet implemented")
    }

    override fun parseStartTime(startTime: String) {
        TODO("Not yet implemented")
    }

    override fun parseConnectingToHostInfo(hostInfo: String) {
        TODO("Not yet implemented")
    }

    override fun parseMode(hostMode: String) {
        TODO("Not yet implemented")
    }

    override fun parseConnectionInfo(connectionInfo: String) {
        TODO("Not yet implemented")
    }

    override fun parseStartingTestInfo(startingTestInfo: String) {
        TODO("Not yet implemented")
    }

    override fun parseTestProgressHeader(testProgressHeader: String) {
        TODO("Not yet implemented")
    }

    override fun parseTestProgress(testProgress: String): IperfTestProgress? {
        val isUploadTest = testProgress.endsWith("Bytes")
        return if (isUploadTest) {
            parseUploadProgress(testProgress)
        } else {
            parseDownloadProgress(testProgress)
        }
    }

    override fun parseTestingPhaseEnd(testingPhaseEnd: String) {
        TODO("Not yet implemented")
    }

    override fun parseEndHeader(testingPhaseEnd: String) {
        TODO("Not yet implemented")
    }

    override fun parseEnd(end: String) {
        TODO("Not yet implemented")
    }

    override fun parseError(error: String) {
        TODO("Not yet implemented")
    }

    override fun parseEndCpuUtil(cpuUtilization: String) {
        TODO("Not yet implemented")
    }

    private fun  parseUploadProgress(progress: String): IperfTestProgress? {
        val pattern =
            """\[\s*(\d+)\]\s+(\d+\.\d+)-(\d+\.\d+)\s+sec\s+(\d+|\d+\.\d+)\s+(Bytes|MBytes|KBytes|GBytes)\s+(\d+|\d+\.\d+)\s+(bits/sec|Mbits/sec|Kbits/sec|Gbits/sec)\s+(\d+)\s+(\d+|\d+\.\d+)\s+(Bytes|MBytes|KBytes|GBytes)""".toRegex()
        val matchResult = pattern.find(progress)

        if (matchResult != null) {
            // Extract the matched groups
            val (id, startTime, endTime, data, dataUnit, speed, speedUnit, retransCount, congestionWindow, congestionWindowUnit) = matchResult.destructured

            Timber.d("ID: $id")
            Timber.d("Start Time: $startTime")
            Timber.d("End Time: $endTime")
            Timber.d("Data Transferred: $data $dataUnit")
            Timber.d("Speed: $speed $speedUnit")
            Timber.d("Retransmission: $retransCount")
            Timber.d("CongestionWindow: $congestionWindow $congestionWindowUnit")

            return IperfTestProgressUpload(
                retransmissions = retransCount.toInt(),
                congestionWindow = congestionWindow.toInt(),
                congestionWindowUnit = congestionWindowUnit,
                timestampMillis = System.currentTimeMillis(),
                relativeTestStartIntervalStart = startTime.toDouble(),
                relativeTestStartIntervalEnd = endTime.toDouble(),
                relativeTestStartIntervalUnit = "sec",
                transferred = data.toDoubleOrNull(),
                transferredUnit = dataUnit,
                bandwidth = speed.toDoubleOrNull(),
                bandwidthUnit = speedUnit
            )
        }
        return null
    }

    private fun  parseDownloadProgress(progress: String): IperfTestProgress? {
        val pattern =
            """\[\s*(\d+)]\s+(\d+\.\d+)-(\d+\.\d+)\s+sec\s+(\d+|\d+\.\d+)\s+(Bytes|MBytes|KBytes|GBytes)\s+(\d+|\d+\.\d+)\s+(bits/sec|Mbits/sec|Kbits/sec|Gbits/sec)""".toRegex()
        val matchResult = pattern.find(progress)

        if (matchResult != null) {
            // Extract the matched groups
            val (id, startTime, endTime, data, dataUnit, speed, speedUnit) = matchResult.destructured

            Timber.d("ID: $id")
            Timber.d("Start Time: $startTime")
            Timber.d("End Time: $endTime")
            Timber.d("Data Transferred: $data $dataUnit")
            Timber.d("Speed: $speed $speedUnit")

            return IperfTestProgressDownload(
                timestampMillis = System.currentTimeMillis(),
                relativeTestStartIntervalStart = startTime.toDouble(),
                relativeTestStartIntervalEnd = endTime.toDouble(),
                relativeTestStartIntervalUnit = "sec",
                transferred = data.toDoubleOrNull(),
                transferredUnit = dataUnit,
                bandwidth = speed.toDoubleOrNull(),
                bandwidthUnit = speedUnit
            )
        }
        return null
    }

}