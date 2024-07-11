package com.cadrikmdev.core.presentation

import android.content.SharedPreferences
import com.cadrikmdev.core.domain.config.Config
import com.cadrikmdev.core.presentation.service.BuildConfig

class AppConfig(
    private val preferences: SharedPreferences
) : Config {

    companion object {
        const val SPEED_TEST_DURATION_SECONDS_DEFAULT = 3600 * 8
        const val SPEED_TEST_PROGRESS_UPDATE_INTERVAL_SECONDS_DEFAULT = 1

        const val DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT = BuildConfig.BASE_URL
        const val DOWNLOAD_SPEED_TEST_SERVER_PORT_DEFAULT = 5201
        const val DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT = 20000000

        const val UPLOAD_SPEED_TEST_SERVER_ADDRESS_DEFAULT = BuildConfig.BASE_URL
        const val UPLOAD_SPEED_TEST_SERVER_PORT_DEFAULT = 5202
        const val UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT = 2000000
    }

    override fun isSpeedTestEnabled(): Boolean {
        return preferences.getBoolean(Config.SPEED_TEST_ENABLED_CONFIG_KEY, false)
    }

    override fun setIsSpeedTestEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(Config.SPEED_TEST_ENABLED_CONFIG_KEY, enabled).apply()
    }

    override fun getIsSpeedTestEnabledDefault(): Boolean {
        return true
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

    override fun setUploadSpeedTestMaxBandwidthBitsPerSecond(maxBandwidthBps: Int) {
        preferences.edit()
            .putInt(Config.UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY, maxBandwidthBps)
            .apply()
    }

    override fun getUploadSpeedTestMaxBandwidthBitsPerSecond(): Int? {
        val maxBandwidth = preferences.getInt(
            Config.UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            Config.UNKNOWN_VALUE
        )
        return if (maxBandwidth == Config.UNKNOWN_VALUE) {
            null
        } else {
            maxBandwidth
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

    override fun setDownloadSpeedTestMaxBandwidth(maxBandwidthBps: Int) {
        preferences.edit().putInt(
            Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            maxBandwidthBps
        ).apply()
    }

    override fun getDownloadSpeedTestMaxBandwidthBitsPerSeconds(): Int? {
        val maxBandwidth = preferences.getInt(
            Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
            Config.UNKNOWN_VALUE
        )
        return if (maxBandwidth == Config.UNKNOWN_VALUE) {
            null
        } else {
            maxBandwidth
        }
    }

    override fun getDownloadSpeedTestMaxBandwidthBitsPerSecondsDefault(): Int {
        return DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_DEFAULT
    }

    override fun setMaxSpeedTestDurationSeconds(durationMillis: Int) {
        preferences.edit().putInt(Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY, durationMillis)
            .apply()
    }

    override fun getSpeedTestDurationSeconds(): Int? {
        val maxDurationMillis =
            preferences.getInt(Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY, Config.UNKNOWN_VALUE)
        return if (maxDurationMillis == Config.UNKNOWN_VALUE) {
            null
        } else {
            maxDurationMillis
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
}