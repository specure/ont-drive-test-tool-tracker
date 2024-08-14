package com.cadrikmdev.permissions.presentation.screen.permissions.mappers

import android.Manifest
import com.cadrikmdev.permissions.presentation.R


fun String.permissionToUiName(): Int {
    return when (this) {
        Manifest.permission.ACCESS_BACKGROUND_LOCATION -> R.string.background_location
        Manifest.permission.ACCESS_COARSE_LOCATION -> R.string.location_permission
        Manifest.permission.ACCESS_FINE_LOCATION -> R.string.location_permission
        Manifest.permission.BLUETOOTH_ADVERTISE -> R.string.bluetooth_advertise
        Manifest.permission.BLUETOOTH_CONNECT -> R.string.bluetooth_connect
        Manifest.permission.POST_NOTIFICATIONS -> R.string.notification_permission
        Manifest.permission.READ_PHONE_STATE -> R.string.read_phone_state
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> R.string.write_external_storage
        else -> -1
    }
}