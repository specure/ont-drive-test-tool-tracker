package com.cadrikmdev.connectivity.presentation.mobile_network.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import timber.log.Timber

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

/**
 * Returns correct telephony manager for data connection or if it fails it returns default one.
 */
fun TelephonyManager.getCorrectDataTelephonyManagerOrDefault(): TelephonyManager {
    return try {
        val dataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
        if (dataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            this.createForSubscriptionId(dataSubscriptionId)
        } else {
            this
        }
    } catch (e: Exception) {
        Timber.e("problem to obtain correct telephony manager for data subscription")
        this
    }
}

fun TelephonyManager.getCorrectDataTelephonyManagerOrNull(): TelephonyManager? {
    return try {
        val dataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
        if (dataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            this.createForSubscriptionId(dataSubscriptionId)
        } else {
            null
        }
    } catch (e: Exception) {
        Timber.e("problem to obtain correct telephony manager for data subscription")
        null
    }
}