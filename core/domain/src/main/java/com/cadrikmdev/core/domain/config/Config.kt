package com.cadrikmdev.core.domain.config

interface Config {

    companion object {
        const val SPEED_TEST_ENABLED_CONFIG_KEY = "SPEED_TEST_ENABLED_CONFIG_KEY"
        const val SPEED_TEST_DURATION_SECONDS_CONFIG_KEY = "SPEED_TEST_DURATION_SECONDS_CONFIG_KEY"
        const val SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_CONFIG_KEY =
            "SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_CONFIG_KEY"
        const val UPLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY =
            "UPLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY"
        const val UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY =
            "UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY"
        const val UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY =
            "UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY"
        const val DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY =
            "DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY"
        const val DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY =
            "DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY"
        const val DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY =
            "DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY"
        const val TRACKING_LOG_INTERVAL_SEC_CONFIG_KEY =
            "TRACKING_LOG_INTERVAL_SEC_CONFIG_KEY"

        const val UNKNOWN_VALUE = Integer.MIN_VALUE
    }

    fun isSpeedTestEnabled(): Boolean

    fun setIsSpeedTestEnabled(enabled: Boolean)

    fun getIsSpeedTestEnabledDefault(): Boolean

    fun setUploadSpeedTestServerAddress(serverAddress: String)

    fun getUploadSpeedTestServerAddress(): String?

    fun getUploadSpeedTestServerAddressDefault(): String

    fun setUploadSpeedTestServerPort(port: Int)

    fun getUploadSpeedTestServerPort(): Int?

    fun getUploadSpeedTestServerPortDefault(): Int

    fun setUploadSpeedTestMaxBandwidthBitsPerSecond(maxBandwidthBitsPerSecond: Int)

    fun getUploadSpeedTestMaxBandwidthBitsPerSecond(): Int?

    fun getUploadSpeedTestMaxBandwidthBitsPerSecondDefault(): Int

    fun setDownloadSpeedTestServerAddress(serverAddress: String)

    fun getDownloadSpeedTestServerAddress(): String?

    fun getDownloadSpeedTestServerAddressDefault(): String

    fun setDownloadSpeedTestServerPort(port: Int)

    fun getDownloadSpeedTestServerPort(): Int?

    fun getDownloadSpeedTestServerPortDefault(): Int

    fun setDownloadSpeedTestMaxBandwidth(maxBandwidthBitsPerSeconds: Int)

    fun getDownloadSpeedTestMaxBandwidthBitsPerSeconds(): Int?

    fun getDownloadSpeedTestMaxBandwidthBitsPerSecondsDefault(): Int

    fun setMaxSpeedTestDurationSeconds(durationSeconds: Int)

    fun getSpeedTestDurationSeconds(): Int?

    fun getSpeedTestDurationSecondsDefault(): Int

    fun setSpeedTestProgressUpdateIntervalSeconds(intervalSeconds: Int)

    fun getSpeedTestProgressUpdateIntervalSeconds(): Int?

    fun getSpeedTestProgressUpdateIntervalSecondsDefault(): Int

    fun setTrackingLogIntervalSeconds(intervalSeconds: Int)

    fun getTrackingLogIntervalSeconds(): Int

    fun getTrackingLogIntervalSecondsDefault(): Int
}
