package com.cadrikmdev.permissions.presentation.screen.permissions.components.dialog

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import com.cadrikmdev.permissions.presentation.R

class ReadPhoneStatePermissionProvider(val context: Context) : PermissionDialogTextProvider {
    override val permissionName: String
        get() = Manifest.permission.READ_PHONE_STATE

    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            ContextCompat.getString(
                context,
                R.string.permission_read_phone_state_rationale
            )
        } else {
            ContextCompat.getString(
                context,
                R.string.permission_read_phone_state_description
            )
        }
    }
}