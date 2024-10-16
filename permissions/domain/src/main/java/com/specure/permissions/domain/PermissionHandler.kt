package com.specure.permissions.domain

import com.specure.permissions.domain.model.Permission

interface PermissionHandler {

    fun setPermissionsNeeded(permissions: LinkedHashMap<String, Permission>)
    fun checkPermissionsState()

    fun updatePermissionStateAfterAsking(permission: String, isGranted: Boolean)

    fun updateMultiplePermissionStateAfterAsking(isPermissionGranted: Map<String, Boolean>)

    fun updateMultipleShouldShowRationale(shouldShowPermissionRationale: Map<String, Boolean>)

    fun updateShouldShowRationale(permission: String, shouldShowRationale: Boolean)

    fun isPermissionPermanentlyDenied(permission: String): Boolean

    fun isPermissionGranted(permission: String): Boolean

    fun isDependentPermissionGranted(permission: String): Boolean

    fun isUpdateForShouldShowRationaleNeeded(): Boolean

    fun getNotGrantedPermissionList(): List<Permission>

    fun getPermissionsToBeAskedList(): List<Permission>
    fun getPermissionsToBeShownRationale(): List<Permission>
}