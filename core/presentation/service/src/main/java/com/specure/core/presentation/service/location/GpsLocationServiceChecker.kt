package com.specure.core.presentation.service.location

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.specure.core.presentation.service.ServiceChecker


class GpsLocationServiceChecker(
    private val context: Context
) : ServiceChecker {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isServiceEnabled(): Boolean {
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {

        }
        return false
    }

    override fun isServiceAvailable(): Boolean {
        return lm.allProviders.contains(LocationManager.GPS_PROVIDER)
    }

    override fun resolve() {
        if (isServiceAvailable()) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
            context.startActivity(intent)
        }
    }
}
