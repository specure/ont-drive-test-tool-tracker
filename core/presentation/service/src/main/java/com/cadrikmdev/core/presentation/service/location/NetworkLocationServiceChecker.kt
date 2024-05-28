package com.cadrikmdev.core.presentation.service.location

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.cadrikmdev.core.presentation.service.ServiceChecker


class NetworkLocationServiceChecker(
    private val context: Context
) : ServiceChecker {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isServiceEnabled(): Boolean {
        try {
            return lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {

        }
        return false
    }

    override fun isServiceAvailable(): Boolean {
        return lm.allProviders.contains(LocationManager.NETWORK_PROVIDER)
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