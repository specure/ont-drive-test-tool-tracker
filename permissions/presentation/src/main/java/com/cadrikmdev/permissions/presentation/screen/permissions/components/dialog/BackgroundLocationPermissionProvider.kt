package com.cadrikmdev.permissions.presentation.screen.permissions.components.dialog

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.cadrikmdev.permissions.presentation.R

class BackgroundLocationPermissionProvider(val context: Context) : PermissionDialogTextProvider {
    override val permissionName: String
        get() = Manifest.permission.ACCESS_BACKGROUND_LOCATION

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            ContextCompat.getString(
                context,
                R.string.permission_background_location_rationale
            )
        } else {
            ContextCompat.getString(
                context,
                R.string.permission_background_location_description
            )
        }
    }
}