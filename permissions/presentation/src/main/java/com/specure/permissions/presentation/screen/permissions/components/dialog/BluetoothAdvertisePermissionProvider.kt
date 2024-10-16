package com.specure.permissions.presentation.screen.permissions.components.dialog

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.specure.permissions.presentation.R

class BluetoothAdvertisePermissionProvider(val context: Context) : PermissionDialogTextProvider {
    override val permissionName: String
        get() = Manifest.permission.BLUETOOTH_ADVERTISE

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            ContextCompat.getString(
                context,
                R.string.permission_bluetooth_advertise_rationale
            )
        } else {
            ContextCompat.getString(
                context,
                R.string.permission_bluetooth_advertise_description
            )
        }
    }
}