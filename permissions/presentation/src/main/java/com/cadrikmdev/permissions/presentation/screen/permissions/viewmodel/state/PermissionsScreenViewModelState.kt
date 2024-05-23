package com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.state

import com.cadrikmdev.permissions.domain.model.Permission

data class PermissionsScreenViewModelState(
    val permission: Permission? = null,
    val permissions: Set<Permission> = setOf(),
    val shouldCheckShowRationaleDialog: Boolean = false,
)