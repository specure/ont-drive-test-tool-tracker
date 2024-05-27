package com.cadrikmdev.permissions.presentation.screen.permissions


sealed class PermissionsScreenEvent {

    class OnPermissionResult(val isPermissionGranted: Map<String, Boolean>) : PermissionsScreenEvent()

    class OnCheckShouldShowRationaleResult(
        val shouldShowRationale: Map<String, Boolean>
    ) : PermissionsScreenEvent()

    data object OnUpdatePermissionState : PermissionsScreenEvent()
}