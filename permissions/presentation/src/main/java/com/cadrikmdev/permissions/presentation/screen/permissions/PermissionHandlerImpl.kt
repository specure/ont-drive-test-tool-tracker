package com.cadrikmdev.permissions.presentation.screen.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.domain.model.Permission

/**
 * Usage:
 *
 * Fill all needed permission with all details and then let manage all permission related stuff in this class
 *
 * Important notice:
 * Permanently denied is always false if you did not ask for permission yet
 * // TODO add should show rationale dialog check
 */
class PermissionHandlerImpl(
    private val context: Context
) : PermissionHandler {

    private var permissions: LinkedHashMap<String, Permission> = linkedMapOf()

    override fun setPermissionsNeeded(permissions: LinkedHashMap<String, Permission>) {
        this.permissions = permissions
        checkPermissionsState()
    }

    override fun checkPermissionsState() {
        permissions.keys.forEach { permission ->
            val isGranted =
                ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            updatePermissionState(permission, isGranted)
        }
    }

    override fun updatePermissionStateAfterAsking(permission: String, isGranted: Boolean) {
        permissions[permission]?.isGranted = isGranted
        permissions[permission]?.isAsked = true
        permissions[permission]?.shouldCheckShouldShowRationale = true
    }

    override fun updateMultiplePermissionStateAfterAsking(isPermissionGranted: Map<String, Boolean>) {
        isPermissionGranted.forEach {
            permissions[it.key]?.isGranted = it.value
            permissions[it.key]?.isAsked = true
            permissions[it.key]?.shouldCheckShouldShowRationale = true
        }
    }

    override fun updateMultipleShouldShowRationale(shouldShowPermissionRationale: Map<String, Boolean>) {
        shouldShowPermissionRationale.forEach {
            permissions[it.key]?.shouldShowRationale = it.value
            permissions[it.key]?.shouldCheckShouldShowRationale = false
        }
    }

    override fun updateShouldShowRationale(permission: String, shouldShowRationale: Boolean) {
        permissions[permission]?.shouldShowRationale = shouldShowRationale
        permissions[permission]?.shouldCheckShouldShowRationale = false
    }

    /**
     * Permanently denied is when is already asked and should show rationale dialog returns false -
     * (!!! should show rationale dialog returns false also before 1st attempt of asking for permission)
     */
    override fun isPermissionPermanentlyDenied(permission: String): Boolean {
        return permissions[permission]?.isAsked == true && permissions[permission]?.shouldShowRationale == false
    }

    override fun isPermissionGranted(permission: String): Boolean {
        val noNeedToAsk = permissions[permission]?.isTargetApiToBeAsked == false
        if (noNeedToAsk) return true
        return permissions[permission]?.isGranted == true
    }

    override fun isDependentPermissionGranted(permission: String): Boolean {
        val permission = permissions[permission]
        if (permission?.dependsOn.isNullOrEmpty()) {
            return true
        }

        val prePermission = try {
            permissions.getValue(permission?.dependsOn!!)
        } catch (e: NoSuchElementException) {
            return true
        }
        prePermission?.let {
            return it.isGranted
        }
        return true
    }

    override fun isUpdateForShouldShowRationaleNeeded(): Boolean {
        return permissions.values.any { permission -> permission.shouldCheckShouldShowRationale }
    }

    override fun getNotGrantedPermissionList(): List<Permission> {
        val permissionsNotGrantedList = mutableListOf<Permission>()
        permissions.keys.forEach { permission ->
//            if (permissions[permission]?.shouldCheckShouldShowRationale == true) {
//                throw Exception("Incorrect permission manager state, please update if it should show rationale")
//            }
            if (!isPermissionGranted(permission) && isDependentPermissionGranted(permission)) {
                permissions[permission]?.let { permissionsNotGrantedList.add(it) }
            }
        }
        return permissionsNotGrantedList
    }

    override fun getPermissionsToBeAskedList(): List<Permission> {
        val permissionsToBeAskedList = mutableListOf<Permission>()
        permissions.keys.forEach { permission ->
//            if (permissions[permission]?.shouldCheckShouldShowRationale == true) {
//                throw Exception("Incorrect permission manager state, please update if it should show rationale")
//            }
            if (!isPermissionGranted(permission) && !isPermissionPermanentlyDenied(permission) && isDependentPermissionGranted(
                    permission
                )
            ) {
                permissions[permission]?.let { permissionsToBeAskedList.add(it) }
            }
        }
        return permissionsToBeAskedList
    }

    override fun getPermissionsToBeShownRationale(): List<Permission> {
        val permissionsToBeShownRationale = mutableListOf<Permission>()
        permissions.keys.forEach { permission ->
//            if (permissions[permission]?.shouldCheckShouldShowRationale == true) {
//                throw Exception("Incorrect permission manager state, please update if it should show rationale")
//            }
            if (!isPermissionGranted(permission) && !isPermissionPermanentlyDenied(permission) && isDependentPermissionGranted(
                    permission
                ) && permissions[permission]?.shouldShowRationale == true
            ) {
                permissions[permission]?.let { permissionsToBeShownRationale.add(it) }
            }
        }
        return permissionsToBeShownRationale
    }

    private fun updatePermissionState(permission: String, isGranted: Boolean) {
        permissions[permission]?.isGranted = isGranted
    }
}