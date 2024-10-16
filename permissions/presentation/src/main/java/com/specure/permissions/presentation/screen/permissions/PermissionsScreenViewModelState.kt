package com.specure.permissions.presentation.screen.permissions

import com.specure.permissions.domain.model.Permission

data class PermissionsScreenViewModelState(
    val permission: Permission? = null,
    val permissions: Set<Permission> = setOf(),
    val shouldCheckShowRationaleDialog: Boolean = false,
)