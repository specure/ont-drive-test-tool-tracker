@file:OptIn(ExperimentalMaterial3Api::class)

package com.specure.permissions.presentation.screen.permissions

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.specure.core.presentation.designsystem.components.SignalTrackerScaffold
import com.specure.core.presentation.designsystem.components.SignalTrackerToolbar
import com.specure.permissions.domain.model.Permission
import com.specure.permissions.presentation.R
import com.specure.permissions.presentation.model.AndroidPermission
import com.specure.permissions.presentation.screen.permissions.components.PermissionRationale
import com.specure.permissions.presentation.screen.permissions.components.dialog.PermissionResolver
import com.specure.permissions.presentation.util.findActivity
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun PermissionsScreen(
    onBackPressed: () -> Unit,
    openAppSettings: () -> Unit,
    viewModel: PermissionsScreenViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        viewModel.actions.collectLatest { action ->
            when (action) {
                is PermissionsScreenAction.OnBackClicked -> {
                    onBackPressed()
                }

                is PermissionsScreenAction.OnOpenAppSettingsClicked -> {
                    openAppSettings()
                }

                else -> Unit
            }
        }
    }

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

    PermissionsScreenContent(
        state.value,
        onBack = { onBackPressed() },
        onPermissionResult = { isPermissionGranted ->
            viewModel.onEvent(
                PermissionsScreenEvent.OnPermissionResult(
                    isPermissionGranted = isPermissionGranted,
                )
            )
        },
        onDismiss = { permission ->
            viewModel.onAction(
                PermissionsScreenAction.OnDismiss(
                    permission
                )
            )
        },
        onOpenAppSettingsClicked = {
            viewModel.onAction(
                PermissionsScreenAction.OnOpenAppSettingsClicked
            )
            openAppSettings()
        },
        onCheckShouldShowRationale = { shouldShowRationale ->
            viewModel.onEvent(
                PermissionsScreenEvent.OnCheckShouldShowRationaleResult(
                    shouldShowRationale
                )
            )
        },
        updatePermissionState = {
            viewModel.onEvent(
                PermissionsScreenEvent.OnUpdatePermissionState
            )
        },
        onResolvePermissionClick = { permission ->
            viewModel.onAction(
                PermissionsScreenAction.OnResolvePermissionClick(
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
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

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

    SignalTrackerScaffold(
        topAppBar = {
            SignalTrackerToolbar(
                showBackButton = true,
                title = stringResource(id = R.string.permissions_needed),
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
                onBackClick = {
                    onBack()
                }
            )
        },
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 8.dp)
        ) {
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