package com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.event.PermissionsScreenEvent
import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.state.PermissionsScreenViewModelStateManager

class PermissionsScreenViewModel(
    private val permissionHandler: PermissionHandler,
    application: Application
) : ViewModel() {

    val stateManager = PermissionsScreenViewModelStateManager()

    val stateFlow
        get() = this.stateManager.stateFlow

    val state
        get() = this.stateFlow.value


//    private val eventChannel = Channel<ActiveTrackEvent>()
//    val events = eventChannel.receiveAsFlow()

    init {
        permissionHandler.setPermissionsNeeded(
            appPermissions
        )
    }

    fun sendScreenToViewModelEvent(screenToViewModelEvent: PermissionsScreenEvent.PermissionsScreenToViewModelEvent) {
        when (screenToViewModelEvent) {
            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnPermissionResult -> {
                stateManager.onPermissionResolved()
                permissionHandler.updateMultiplePermissionStateAfterAsking(screenToViewModelEvent.isPermissionGranted)
                stateManager.setUpdateShowRationale(permissionHandler.isUpdateForShouldShowRationaleNeeded())
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnDismiss -> {
                permissionHandler.updatePermissionStateAfterAsking(
                    screenToViewModelEvent.permission,
                    false
                )
                stateManager.onPermissionResolved()
                stateManager.setUpdateShowRationale(permissionHandler.isUpdateForShouldShowRationaleNeeded())
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnOpenAppSettingsClicked -> {
                stateManager.onPermissionResolved()
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnCheckShouldShowRationaleResult -> {
                permissionHandler.updateMultipleShouldShowRationale(screenToViewModelEvent.shouldShowRationale)
                stateManager.setPermissionsToAsk(
                    permissionHandler.getNotGrantedPermissionList().toSet()
                )
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnUpdatePermissionState -> {
                checkPermissionsStatus()
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnResolvePermissionClick -> {
                stateManager.setPermissionToResolve(screenToViewModelEvent.permission)
            }

            is PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnBackClicked -> TODO()
        }
    }

    private fun checkPermissionsStatus() {
        permissionHandler.checkPermissionsState()
        stateManager.setPermissionsToAsk(permissionHandler.getNotGrantedPermissionList().toSet())
    }

}