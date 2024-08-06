@file:OptIn(ExperimentalMaterial3Api::class)

package com.cadrikmdev.track.presentation.active_track

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.StartIcon
import com.cadrikmdev.core.presentation.designsystem.StopIcon
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerDialog
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerFloatingActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.core.presentation.ui.ObserveAsEvents
import com.cadrikmdev.track.presentation.R
import com.cadrikmdev.track.presentation.active_track.components.TrackDataCard
import com.cadrikmdev.track.presentation.active_track.maps.TrackerMap
import com.cadrikmdev.track.presentation.active_track.service.ActiveTrackService
import com.cadrikmdev.track.presentation.util.hasLocationPermission
import com.cadrikmdev.track.presentation.util.hasNotificationPermission
import com.cadrikmdev.track.presentation.util.shouldShowLocationPermissionRationale
import com.cadrikmdev.track.presentation.util.shouldShowNotificationPermissionRationale
import org.koin.androidx.compose.koinViewModel

@Composable
fun ActiveTrackScreenRoot(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    onServiceToggle: (isServiceRunning: Boolean) -> Unit,
    viewModel: ActiveTrackViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ActiveTrackEvent.Error -> {
                Toast.makeText(
                    context,
                    event.error.asString(context),
                    Toast.LENGTH_LONG
                ).show()
            }

            ActiveTrackEvent.TrackSaved -> onFinish()
            else -> viewModel::onEvent
        }
    }

    ActiveTrackScreen(
        state = viewModel.state,
        onServiceToggle = onServiceToggle,
        onAction = { action ->
            when (action) {
                ActiveTrackAction.OnBackClick -> {
                    if (!viewModel.state.hasStartedTracking) {
                        onBack()
                    }
                }

                else -> Unit
            }
            viewModel.onAction(action)
        },
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ActiveTrackScreen(
    state: ActiveTrackState,
    onServiceToggle: (isServiceRunning: Boolean) -> Unit,
    onAction: (ActiveTrackAction) -> Unit,
    onEvent: (ActiveTrackEvent) -> Unit
) {
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                onEvent(ActiveTrackEvent.OnUpdatePermissionStatus)
            }
            Lifecycle.State.DESTROYED,
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED,
            Lifecycle.State.STARTED -> { // nothing to do }
            }
        }
    }


    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val hasCourseLocationPermission =
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val hasFineLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val hasNotificationPermission = if (Build.VERSION.SDK_INT >= 33) {
                perms[Manifest.permission.POST_NOTIFICATIONS] == true
            } else true
            val activity = context as ComponentActivity
            val showLocationRationale = activity.shouldShowLocationPermissionRationale()
            val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

            onAction(
                ActiveTrackAction.SubmitLocationPermissionInfo(
                    acceptedLocationPermission = hasCourseLocationPermission && hasFineLocationPermission,
                    showLocationRationale = showLocationRationale
                )
            )

            onAction(
                ActiveTrackAction.SubmitNotificationPermissionInfo(
                    acceptedNotificationPermission = hasNotificationPermission,
                    showNotificationRationale = showNotificationRationale
                )
            )
        }
    LaunchedEffect(key1 = true) {
        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            ActiveTrackAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = context.hasLocationPermission(),
                showLocationRationale = showLocationRationale
            )
        )

        onAction(
            ActiveTrackAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = context.hasNotificationPermission(),
                showNotificationRationale = showNotificationRationale
            )
        )
        if (!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestSignalTrackerPermissions(context)

        }
    }

    LaunchedEffect(key1 = state.isTrackFinished) {
        if (state.isTrackFinished) {
            onServiceToggle(false)
        }
    }


    LaunchedEffect(key1 = state.shouldTrack) {
        if (context.hasLocationPermission() && state.shouldTrack && !ActiveTrackService.isServiceActive) {
            onServiceToggle(true)
        }
    }

    SignalTrackerScaffold(
        withGradient = false,
        topAppBar = {
            SignalTrackerToolbar(
                showBackButton = true,
                title = stringResource(id = R.string.active_track),
                onBackClick = {
                    onAction(ActiveTrackAction.OnBackClick)
                },
            )
        },
        floatingActionButton = {
            SignalTrackerFloatingActionButton(
                icon = if (state.shouldTrack) {
                    StopIcon
                } else {
                    StartIcon
                },
                onClick = {
                    onAction(ActiveTrackAction.OnToggleTrackClick)
                },
                iconSize = 20.dp,
                contentDescription = if (state.shouldTrack) {
                    stringResource(id = R.string.pause_track)
                } else {
                    stringResource(id = R.string.start_track)
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TrackerMap(
                isTrackFinished = false,
                currentLocation = state.currentLocation,
                locations = state.trackData.locations,
                onSnapshot = { },
                modifier = Modifier
                    .fillMaxSize()
            )
            TrackDataCard(
                elapsedTime = state.elapsedTime,
                trackData = state.trackData,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(padding)
                    .fillMaxWidth()
            )
        }
    }

    if (state.isShowingFinishConfirmationDialog && state.hasStartedTracking) {
        SignalTrackerDialog(
            title = stringResource(
                id = R.string.want_finish_tracking
        ),
            onDismiss = {
                onAction(ActiveTrackAction.OnResumeTrackClick)
            },
            description = stringResource(id = R.string.resume_or_finish_track),
            primaryButton = {
                SignalTrackerActionButton(
                    text = stringResource(id = R.string.resume),
                    isLoading = false,
                    onClick = {
                        onAction(ActiveTrackAction.OnResumeTrackClick)
                    },
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                SignalTrackerOutlinedActionButton(
                    text = stringResource(id = R.string.finish),
                    isLoading = state.isSavingTrack,
                    onClick = {
                        onAction(ActiveTrackAction.OnFinishTrackClick)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }
}

private fun ActivityResultLauncher<Array<String>>.requestSignalTrackerPermissions(
    context: Context
) {
    val hasLocationPermission = context.hasLocationPermission()
    val hasNotificationPermission = context.hasNotificationPermission()

    val locationPermission = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val notificationPermission = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else arrayOf()

    when {
        !hasLocationPermission && !hasNotificationPermission -> {
            launch(locationPermission + notificationPermission)
        }

        !hasLocationPermission -> launch(locationPermission)
        !hasNotificationPermission -> launch(notificationPermission)
    }
}

@Preview
@Composable
private fun ActiveTrackScreenPreview() {
    SignalTrackerTheme {
        ActiveTrackScreen(
            state = ActiveTrackState(),
            onServiceToggle = {},
            onAction = {},
            onEvent = {},
        )
    }
}