package com.cadrikmdev.permissions.presentation.screen.permissions

import android.app.Application
import androidx.lifecycle.ViewModel
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.appPermissions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class PermissionsScreenViewModel(
    private val permissionHandler: PermissionHandler,
    application: Application
) : ViewModel() {

    private val stateManager = PermissionsScreenViewModelStateManager()

    val stateFlow
        get() = this.stateManager.stateFlow

    val state
        get() = this.stateFlow.value


    private val actonChannel = Channel<PermissionsScreenAction>()
    val actions = actonChannel.receiveAsFlow()

    private val eventChannel = Channel<PermissionsScreenEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        stateManager.setInitialState()

        permissionHandler.setPermissionsNeeded(
            appPermissions
        )
    }

    fun onAction(action: PermissionsScreenAction) {
        when (action) {
            is PermissionsScreenAction.OnDismiss -> {
                permissionHandler.updatePermissionStateAfterAsking(
                    action.permission,
                    false
                )
                stateManager.onPermissionResolved()
                stateManager.setUpdateShowRationale(permissionHandler.isUpdateForShouldShowRationaleNeeded())
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            PermissionsScreenAction.OnOpenAppSettingsClicked -> {
                stateManager.onPermissionResolved()
            }

            is PermissionsScreenAction.OnResolvePermissionClick -> {
                stateManager.setPermissionToResolve(action.permission)
            }

            PermissionsScreenAction.OnBackClicked -> {

            }
        }
    }

    fun onEvent(event: PermissionsScreenEvent) {
        when (event) {
            is PermissionsScreenEvent.OnCheckShouldShowRationaleResult -> {
                permissionHandler.updateMultipleShouldShowRationale(event.shouldShowRationale)
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            is PermissionsScreenEvent.OnPermissionResult -> {
                stateManager.onPermissionResolved()
                permissionHandler.updateMultiplePermissionStateAfterAsking(event.isPermissionGranted)
                stateManager.setUpdateShowRationale(permissionHandler.isUpdateForShouldShowRationaleNeeded())
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            PermissionsScreenEvent.OnUpdatePermissionState -> checkPermissionsStatus()
        }
    }

    private fun checkPermissionsStatus() {
        permissionHandler.checkPermissionsState()
        stateManager.setPermissionsToAsk(permissionHandler.getNotGrantedPermissionList().toSet())
    }

}