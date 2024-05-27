package com.cadrikmdev.permissions.presentation.screen.permissions

import com.cadrikmdev.permissions.domain.model.Permission


sealed class PermissionsScreenAction {


    class OnDismiss(val permission: String) :
        PermissionsScreenAction()

    data object OnOpenAppSettingsClicked :
        PermissionsScreenAction()

    class OnResolvePermissionClick(
        val permission: Permission
    ) : PermissionsScreenAction()

    data object OnBackClicked : PermissionsScreenAction()
}