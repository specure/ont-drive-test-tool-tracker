package com.cadrikmdev.track.domain.config

interface Config {

    companion object {
        const val SPEED_TEST_ENABLED_CONFIG_KEY = "SPEED_TEST_ENABLED_CONFIG_KEY"
    }

    fun isSpeedTestEnabled(): Boolean

    fun setIsSpeedTestEnabled(enabled: Boolean)
}
