package com.cadrikmdev.permissions.presentation.screen.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import com.cadrikmdev.permissions.domain.model.Permission
import com.cadrikmdev.permissions.presentation.AndroidPermission
import com.cadrikmdev.permissions.presentation.screen.permissions.components.PermissionRationale
import com.cadrikmdev.permissions.presentation.screen.permissions.components.dialog.PermissionResolver
import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.PermissionsScreenViewModel
import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.event.PermissionsScreenEvent
import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.state.PermissionsScreenViewModelState
import com.cadrikmdev.permissions.presentation.util.findActivity
import org.koin.androidx.compose.koinViewModel

@Composable
fun PermissionsScreen(
    onBackPressed: () -> Unit,
    openAppSettings: () -> Unit,
    viewModel: PermissionsScreenViewModel = koinViewModel(),
) {
//    val lifecycleOwner = LocalLifecycleOwner.current
//    LaunchedEffect(lifecycleOwner.lifecycle) {
//        viewModel.viewModelToActivityEventFlow.collectLatest { event ->
//            when (event) {
//                is PermissionsScreenEvent.PermissionsViewModelToActivityEvent.OnBack -> {
//                    onBackPressed()
//                }
//
//                PermissionsScreenEvent.PermissionsViewModelToActivityEvent.OnOpenAppSettings -> {
//                    openAppSettings()
//                }
//            }
//        }
//    }

    PermissionsScreenContent(
        viewModel.state,
        onBack = { onBackPressed() },
        onPermissionResult = { isPermissionGranted ->
            viewModel.sendScreenToViewModelEvent(
                PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnPermissionResult(
                    isPermissionGranted = isPermissionGranted,
                )
            )
        },
        onDismiss = { permission ->
            viewModel.sendScreenToViewModelEvent(
                PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnDismiss(
                    permission
                )
            )
        },
        onOpenAppSettingsClicked = {
            openAppSettings()
        },
        onCheckShouldShowRationale = { shouldShowRationale ->
            viewModel.sendScreenToViewModelEvent(
                PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnCheckShouldShowRationaleResult(
                    shouldShowRationale
                )
            )
        },
        updatePermissionState = {
            viewModel.sendScreenToViewModelEvent(
                PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnUpdatePermissionState
            )
        },
        onResolvePermissionClick = { permission ->
            viewModel.sendScreenToViewModelEvent(
                PermissionsScreenEvent.PermissionsScreenToViewModelEvent.OnResolvePermissionClick(
                    permission
                )
            )
        }
    )
}

@Composable
fun PermissionsScreenContent(
    state: PermissionsScreenViewModelState,
    onBack: () -> Unit,
    onPermissionResult: (isPermissionGranted: Map<String, Boolean>) -> Unit,
    onDismiss: (permissionName: String) -> Unit,
    onOpenAppSettingsClicked: () -> Unit,
    onCheckShouldShowRationale: (shouldShowRationale: Map<String, Boolean>) -> Unit,
    updatePermissionState: () -> Unit,
    onResolvePermissionClick: (permission: Permission) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {

        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                updatePermissionState()
            }

            Lifecycle.State.DESTROYED,
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED,
            Lifecycle.State.STARTED -> { // nothing to do }
            }
        }
    }

    LaunchedEffect(state.shouldCheckShowRationaleDialog) {
        val shouldShowRationaleMap = HashMap<String, Boolean>()
        state.permissions.forEach { permission ->
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                permission.name
            )
            shouldShowRationaleMap[permission.name] = shouldShowRationale
        }
        onCheckShouldShowRationale(shouldShowRationaleMap)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(text = "Permissions denied")
        LazyColumn {
            items(state.permissions.size) { i ->
                val permission = state.permissions.elementAt(i)
                PermissionRationale(
                    permission = permission,
                    onResolveClick = {
                        onResolvePermissionClick(permission)
                    }
                )
            }
        }
    }

    PermissionResolver(
        permission = state.permission,
        onPermissionResult = onPermissionResult,
        onDismiss = onDismiss,
        onOpenAppSettingsClicked = onOpenAppSettingsClicked,
    )

}

@Preview(showSystemUi = true)
//@PreviewScreenSizes
//@PreviewFontScale
//@PreviewLightDark
//@PreviewDynamicColors
@Composable
fun PermissionsScreenPreview(@PreviewParameter(PermissionsScreenPreviewParameterProvider::class) state: PermissionsScreenViewModelState) {
    PermissionsScreenContent(
        state,
        {},
        { _ -> {} },
        { _ -> {} },
        {},
        { _ -> {} },
        {},
        { _ -> }
    )
}

class PermissionsScreenPreviewParameterProvider :
    PreviewParameterProvider<PermissionsScreenViewModelState> {
    @RequiresApi(Build.VERSION_CODES.Q)
    override val values = sequenceOf(
        PermissionsScreenViewModelState(
            permissions = setOf(
                AndroidPermission(name = Manifest.permission.ACCESS_FINE_LOCATION),
                AndroidPermission(
                    name = Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    minimumApiRequired = Build.VERSION_CODES.Q
                ),
                AndroidPermission(
                    name = Manifest.permission.READ_PHONE_STATE,
                    minimumApiRequired = Build.VERSION_CODES.Q
                )
            )
        )
    )
}