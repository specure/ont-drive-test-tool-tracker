package com.cadrikmdev.core.presentation.service.location

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.cadrikmdev.core.presentation.service.ServiceChecker


class FusedLocationServiceChecker(
    private val context: Context
) : ServiceChecker {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val laterThanApiLevelS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    override fun isServiceEnabled(): Boolean {
        if (laterThanApiLevelS) {
            try {
                return lm.isProviderEnabled(LocationManager.FUSED_PROVIDER)
            } catch (_: Exception) {

            }
        }
        return false
    }

    override fun isServiceAvailable(): Boolean {
        if (laterThanApiLevelS) {
            return lm.allProviders.contains(LocationManager.FUSED_PROVIDER)
        }
        return false
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