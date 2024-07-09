package com.cadrikmdev.track.data

import android.content.SharedPreferences
import com.cadrikmdev.track.domain.config.Config

class AppConfig(
    private val preferences: SharedPreferences
) : Config {

    override fun isSpeedTestEnabled(): Boolean {
        return preferences.getBoolean(Config.SPEED_TEST_ENABLED_CONFIG_KEY, false)
    }

    override fun setIsSpeedTestEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(Config.SPEED_TEST_ENABLED_CONFIG_KEY, enabled).apply()
    }
}