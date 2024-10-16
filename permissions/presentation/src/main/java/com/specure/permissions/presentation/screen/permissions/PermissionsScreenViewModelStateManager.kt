package com.specure.permissions.presentation.screen.permissions

import com.specure.permissions.domain.model.Permission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PermissionsScreenViewModelStateManager() {

    private val permissionsViewModelState: MutableStateFlow<PermissionsScreenViewModelState> by lazy {
        MutableStateFlow(
            this.setInitialState()
        )
    }

    val stateFlow = this.permissionsViewModelState.asStateFlow()

    val state
        get() = stateFlow.value

    fun setInitialState() = PermissionsScreenViewModelState()


    fun setPermissionsToAsk(permissions: Set<Permission>) {
        this.permissionsViewModelState.update { state ->
            state.copy(
                permissions = permissions
            )
        }
    }

    fun setUpdateShowRationale(updateForShouldShowRationaleNeeded: Boolean) {
        this.permissionsViewModelState.update { state ->
            state.copy(
                shouldCheckShowRationaleDialog = updateForShouldShowRationaleNeeded
            )
        }
    }

    fun setPermissionToResolve(permission: Permission) {
        this.permissionsViewModelState.update { state ->
            state.copy(
                permission = permission
            )
        }
    }

    fun onPermissionResolved() {
        this.permissionsViewModelState.update { state ->
            state.copy(
                permission = null
            )
        }
    }
}