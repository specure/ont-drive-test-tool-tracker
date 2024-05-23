package com.cadrikmdev.permissions.presentation.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


/**
 * Checks that runtime permission is allowed by user
 */
fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * @return true if [Manifest.permission.ACCESS_FINE_LOCATION] permissions
 * are granted
 */
fun Context.isFineLocationPermitted() =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

/**
 * @return true if [Manifest.permission.ACCESS_FINE_LOCATION] or [Manifest.permission.ACCESS_COARSE_LOCATION] permissions
 * are granted
 */
fun Context.isCoarseLocationPermitted() =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

/**
 * @return true if [Manifest.permission.READ_PHONE_STATE] permission is granted
 */
fun Context.isReadPhoneStatePermitted() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

internal fun Activity.shouldShowRationale(permissions: List<String>) =
    permissions.any { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

/**
 * Opens Current application system settings
 */
fun Context.openAppSettings() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

internal fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    error("Current context isn't an Activity")
}