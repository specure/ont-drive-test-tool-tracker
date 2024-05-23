package com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel

import android.Manifest
import android.os.Build
import com.cadrikmdev.permissions.domain.model.Permission
import com.cadrikmdev.permissions.presentation.AndroidPermission

val appPermissions = linkedMapOf<String, Permission>(
    Pair(
        Manifest.permission.ACCESS_FINE_LOCATION,
        AndroidPermission(name = Manifest.permission.ACCESS_FINE_LOCATION)
    ),
    Pair(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        AndroidPermission(
            dependsOn = Manifest.permission.ACCESS_FINE_LOCATION,
            name = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            minimumApiRequired = Build.VERSION_CODES.Q
        )
    ),
    Pair(
        Manifest.permission.READ_PHONE_STATE,
        AndroidPermission(
            name = Manifest.permission.READ_PHONE_STATE,
        )
    ),
    Pair(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        AndroidPermission(
            name = Manifest.permission.READ_EXTERNAL_STORAGE,
            minimumApiRequired = Build.VERSION_CODES.M,
            maximumApiRequired = Build.VERSION_CODES.P,
        )
    ),
    Pair(
        Manifest.permission.POST_NOTIFICATIONS,
        AndroidPermission(
            name = Manifest.permission.POST_NOTIFICATIONS,
            minimumApiRequired = Build.VERSION_CODES.TIRAMISU,
        )
    ),
)