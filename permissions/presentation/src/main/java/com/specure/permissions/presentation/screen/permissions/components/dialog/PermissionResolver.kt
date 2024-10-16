package com.specure.permissions.presentation.screen.permissions.components.dialog

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.specure.permissions.domain.model.Permission
import com.specure.permissions.presentation.model.AndroidPermission
import com.specure.permissions.presentation.util.findActivity
import timber.log.Timber

@Composable
fun PermissionResolver(
    permission: Permission?,
    onPermissionResult: (permissionResult: Map<String, Boolean>) -> Unit,
    onDismiss: (permission: String) -> Unit,
    onOpenAppSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val singlePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (permission != null) {
                onPermissionResult(mapOf(Pair(permission.name, isGranted)))
            }
        },
    )

    if (permission != null)
        if (permission?.isAsked == false) {
            singlePermissionLauncher.launch(permission?.name!!)
        } else {
            val shouldShowRationale = !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission.name
            )
            Timber.d("Showing dialog for ${permission.name}")
            PermissionDialog(
                permission = when (permission.name) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        FineLocationPermissionProvider(activity)
                    }

                    Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        BackgroundLocationPermissionProvider(activity)
                    }

                    Manifest.permission.READ_PHONE_STATE -> {
                        ReadPhoneStatePermissionProvider(activity)
                    }

                    else -> {
                        UnknownPermissionProvider()
                    }
                },
                isPermanentlyDeclined = shouldShowRationale && permission.isAsked,
                onDismiss = {
                    onDismiss(permission.name)
                    Timber.d("Dismiss clicked dialog for ${permission.name}")
                },
                onOkClick = {
                    Timber.d("Ok clicked dialog for ${permission.name}")
                    singlePermissionLauncher.launch(
                        permission.name
                    )
                },
                onGoToAppSettingsClick = {
                    onOpenAppSettingsClicked()
                },
                modifier = modifier,
            )
        }
}


@Preview
@Composable
fun previewPermissionResolver() {
    PermissionResolver(
        permission = AndroidPermission(
            Manifest.permission.READ_PHONE_STATE,
        ),
        onPermissionResult = { permission -> },
        onOpenAppSettingsClicked = {},
        onDismiss = {},
        modifier = Modifier.fillMaxWidth()
    )
}