package com.cadrikmdev.core.presentation.service.location

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.cadrikmdev.core.presentation.service.ServiceChecker

class GeneralLocationServiceChecker(
    private val context: Context
) : ServiceChecker {
    override fun isServiceEnabled(): Boolean {
        try {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // This is a new method provided in API 28
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                lm.isLocationEnabled
            } else {
                // This was deprecated in API 28
                val mode = Settings.Secure.getInt(
                    context.contentResolver, Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
                )
                mode != Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (_: Exception) {
        }
        return false
    }

    override fun isServiceAvailable(): Boolean {
        return true
    }

    override fun resolve() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.addFlags(
            FLAG_ACTIVITY_NEW_TASK
        )
        context.startActivity(intent)
    }
}