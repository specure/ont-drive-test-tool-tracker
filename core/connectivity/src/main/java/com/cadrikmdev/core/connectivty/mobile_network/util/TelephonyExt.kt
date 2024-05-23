package com.cadrikmdev.core.connectivty.mobile_network.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat

/**
 * Checks if current device is running in dual sim mode or single sim mode
 */
fun Context.isDualSim(telephonyManager: TelephonyManager, subscriptionManager: SubscriptionManager): Boolean {
    return if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        telephonyManager.phoneCount > 1
    } else {
        subscriptionManager.activeSubscriptionInfoCount > 1
    }
}