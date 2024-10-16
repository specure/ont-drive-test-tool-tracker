package com.specure.core.presentation

import android.content.SharedPreferences
import com.specure.core.domain.config.Config

class AppConfig(
    private val preferences: SharedPreferences
) : Config {

    companion object {
        const val FEATURE_SPEED_TEST_ENABLED =
            com.specure.core.presentation.service.BuildConfig.FEATURE_SPEED_TEST_ENABLED
        const val IS_SPEED_TEST_ENABLED_BY_DEFAULT = true

        const val SPEED_TEST_DURATION_SECONDS_DEFAULT = 3600 * 8
        const val SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_DEFAULT = 1

        const val DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT =
            com.specure.core.presentation.service.BuildConfig.BASE_URL
        const val DOWNLOAD_SPEED_TEST_SERVER_PORT_DEFAULT = 5201
        const val DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT = 20000000

        const val UPLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT =
            com.specure.core.presentation.service.BuildConfig.BASE_URL
        const val UPLOAD_SPEED_TEST_SERVER_PORT_DEFAULT = 5202
        const val UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT = 2000000

        const val TRACKING_LOG_INTERVAL_SECONDS = 1
    }

    override fun isSpeedTestEnabled(): Boolean {
        return preferences.getBoolean(
            Config.SPEED_TEST_ENABLED_CONFIG_KEY,
            getIsSpeedTestEnabledDefault()
        ) && FEATURE_SPEED_TEST_ENABLED
    }

    override fun setIsSpeedTestEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(Config.SPEED_TEST_ENABLED_CONFIG_KEY, enabled).apply()
    }

    override fun getIsSpeedTestEnabledDefault(): Boolean {
        return IS_SPEED_TEST_ENABLED_BY_DEFAULT
    }

    override fun setUploadSpeedTestServerAddress(serverAddress: String) {
        preferences.edit()
            .putString(Config.UPLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY, serverAddress).apply()
    }

    override fun getUploadSpeedTestServerAddress(): String? {
        return preferences.getString(Config.UPLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY, null)
    }

    override fun getUploadSpeedTestServerAddressDefault(): String {
        return UPLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT
    }

    override fun setUploadSpeedTestServerPort(port: Int) {
        preferences.edit().putInt(Config.UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY, port).apply()
    }

    override fun getUploadSpeedTestServerPort(): Int? {
        val port = preferences.getInt(
            Config.UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
            Config.UNKNOWN_VALUE
        )
        return if (port == Config.UNKNOWN_VALUE) {
            null
        } else {
            port
        }
    }

    override fun getUploadSpeedTestServerPortDefault(): Int {
        return UPLOAD_SPEED_TEST_SERVER_PORT_DEFAULT
    }

    override fun setUploadSpeedTestMaxBandwidthBitsPerSecond(maxBandwidthBitsPerSecond: Int) {
        preferences.edit()
            .putInt(
                Config.UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
                maxBandwidthBitsPerSecond
            )
            .apply()
    }

    override fun getUploadSpeedTestMaxBandwidthBitsPerSecond(): Int? {
        val maxBandwidth = preferences.getFloat(
            Config.UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            Config.UNKNOWN_VALUE.toFloat()
        )
        return if (maxBandwidth == Config.UNKNOWN_VALUE.toFloat()) {
            null
        } else {
            maxBandwidth.toInt()
        }
    }

    override fun getUploadSpeedTestMaxBandwidthBitsPerSecondDefault(): Int {
        return UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT
    }

    override fun setDownloadSpeedTestServerAddress(serverAddress: String) {
        preferences.edit()
            .putString(Config.DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY, serverAddress).apply()
    }

    override fun getDownloadSpeedTestServerAddress(): String? {
        return preferences.getString(Config.DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY, null)
    }

    override fun getDownloadSpeedTestServerAddressDefault(): String {
        return DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT
    }

    override fun setDownloadSpeedTestServerPort(port: Int) {
        preferences.edit().putInt(Config.DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY, port).apply()
    }

    override fun getDownloadSpeedTestServerPort(): Int? {
        val port = preferences.getInt(
            Config.DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
            Config.UNKNOWN_VALUE
        )
        return if (port == Config.UNKNOWN_VALUE) {
            null
        } else {
            port
        }
    }

    override fun getDownloadSpeedTestServerPortDefault(): Int {
        return DOWNLOAD_SPEED_TEST_SERVER_PORT_DEFAULT
    }

    override fun setDownloadSpeedTestMaxBandwidth(maxBandwidthBitsPerSecond: Int) {
        preferences.edit().putFloat(
            Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            maxBandwidthBitsPerSecond.toFloat()
        ).apply()
    }

    override fun getDownloadSpeedTestMaxBandwidthBitsPerSeconds(): Int? {
        val maxBandwidth = preferences.getFloat(
            Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            Config.UNKNOWN_VALUE.toFloat()
        )
        return if (maxBandwidth == Config.UNKNOWN_VALUE.toFloat()) {
            null
        } else {
            maxBandwidth.toInt()
        }
    }

    override fun getDownloadSpeedTestMaxBandwidthBitsPerSecondsDefault(): Int {
        return DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT
    }

    override fun setMaxSpeedTestDurationSeconds(durationSeconds: Int) {
        preferences.edit().putInt(Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY, durationSeconds)
            .apply()
    }

    override fun getSpeedTestDurationSeconds(): Int? {
        val maxDurationSeconds =
            preferences.getInt(Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY, Config.UNKNOWN_VALUE)
        return if (maxDurationSeconds == Config.UNKNOWN_VALUE) {
            null
        } else {
            maxDurationSeconds
        }
    }

    override fun getSpeedTestDurationSecondsDefault(): Int {
        return SPEED_TEST_DURATION_SECONDS_DEFAULT
    }

    override fun setSpeedTestProgressUpdateIntervalSeconds(intervalSeconds: Int) {
        preferences.edit()
            .putInt(Config.SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_CONFIG_KEY, intervalSeconds)
            .apply()
    }

    override fun getSpeedTestProgressUpdateIntervalSeconds(): Int? {
        val updateIntervalMillis = preferences.getInt(
            Config.SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_CONFIG_KEY,
            Config.UNKNOWN_VALUE
        )
        return if (updateIntervalMillis == Config.UNKNOWN_VALUE) {
            null
        } else {
            updateIntervalMillis
        }
    }

    override fun getSpeedTestProgressUpdateIntervalSecondsDefault(): Int {
        return SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_DEFAULT
    }

    override fun setTrackingLogIntervalSeconds(intervalSeconds: Int) {
        preferences.edit()
            .putFloat(Config.TRACKING_LOG_INTERVAL_SEC_CONFIG_KEY, intervalSeconds.toFloat())
            .apply()
    }

    override fun getTrackingLogIntervalSeconds(): Int {
        val logIntervalMillis = preferences.getFloat(
            Config.TRACKING_LOG_INTERVAL_SEC_CONFIG_KEY,
            getTrackingLogIntervalSecondsDefault().toFloat()
        )
        return logIntervalMillis.toInt()
    }

    override fun getTrackingLogIntervalSecondsDefault(): Int {
        return TRACKING_LOG_INTERVAL_SECONDS
    }
}