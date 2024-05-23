package com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.event

import com.cadrikmdev.permissions.domain.model.Permission


sealed class PermissionsScreenEvent {

    sealed class PermissionsScreenToViewModelEvent {
        class OnPermissionResult(val isPermissionGranted: Map<String, Boolean>) :
            PermissionsScreenToViewModelEvent()

        class OnDismiss(val permission: String) :
            PermissionsScreenToViewModelEvent()

        data object OnOpenAppSettingsClicked :
            PermissionsScreenToViewModelEvent()

        class OnCheckShouldShowRationaleResult(
            val shouldShowRationale: Map<String, Boolean>
        ) : PermissionsScreenToViewModelEvent()

        class OnResolvePermissionClick(
            val permission: Permission
        ) : PermissionsScreenToViewModelEvent()

        data object OnUpdatePermissionState : PermissionsScreenToViewModelEvent()

        data object OnBackClicked : PermissionsScreenToViewModelEvent()
    }

    sealed class PermissionsViewModelToActivityEvent {
        class OnBack : PermissionsViewModelToActivityEvent()

        data object OnOpenAppSettings : PermissionsViewModelToActivityEvent()
    }

    sealed class ActivityToPermissionsViewModelEvent {
        class OnShouldShowRationaleResult(
            val permission: String,
            val shouldShowRationale: Boolean
        ) :
            ActivityToPermissionsViewModelEvent()
    }
}