@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.cadrikmdev.track.presentation.track_overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.cadrikmdev.core.presentation.designsystem.LogoIcon
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.TrackIcon
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerFloatingActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.track.presentation.R
import com.cadrikmdev.track.presentation.track_overview.components.TrackListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackOverviewScreenRoot(
    onStartTrackClick: () -> Unit,
    onResolvePermissionClick: () -> Unit,
    viewModel: TrackOverviewViewModel = koinViewModel(),
) {
    TrackOverviewScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                TrackOverviewAction.OnStartClick -> onStartTrackClick()
                TrackOverviewAction.OnResolvePermissionClick -> onResolvePermissionClick()
                else -> Unit
            }
            viewModel.onAction(action)
        },
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun TrackOverviewScreen(
    state: TrackOverviewState,
    onAction: (TrackOverviewAction) -> Unit,
    onEvent: (TrackOverviewEvent) -> Unit
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.RESUMED -> {
                onEvent(TrackOverviewEvent.OnUpdatePermissionStatus)
                onEvent(TrackOverviewEvent.OnUpdateLocationServiceStatus)
            }

            Lifecycle.State.DESTROYED,
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED,
            Lifecycle.State.STARTED -> { // nothing to do }
            }
        }
    }

    SignalTrackerScaffold(
        topAppBar = {
            SignalTrackerToolbar(
                showBackButton = false,
                title = stringResource(id = R.string.signaltracker),
                scrollBehavior = scrollBehavior,
                startContent = {
                    Icon(
                        imageVector = LogoIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                },
            )
        },
        floatingActionButton = {
            SignalTrackerFloatingActionButton(
                icon = TrackIcon,
                onClick = { onAction(TrackOverviewAction.OnStartClick) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.internetConnection))
                if (state.isOnline)
                    Text(
                        text = stringResource(id = R.string.available)
                    )
                else
                    Text(
                        text = stringResource(id = R.string.unavailable),
                        color = MaterialTheme.colorScheme.error
                    )

            }
            if (state.isPermissionRequired) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(id = R.string.permission_required))
                    if (state.isPermissionRequired) {
                        SignalTrackerOutlinedActionButton(
                            modifier = Modifier.padding(start = 16.dp),
                            text = stringResource(id = com.cadrikmdev.permissions.presentation.R.string.resolve),
                            isLoading = false
                        ) {
                            onAction(TrackOverviewAction.OnResolvePermissionClick)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(id = R.string.location_service))
                if (state.isLocationServiceEnabled) {
                    Text(
                        text = stringResource(id = R.string.available)
                    )
                } else if (state.isLocationServiceResolvable) {
                    SignalTrackerOutlinedActionButton(
                        modifier = Modifier.padding(start = 16.dp),
                        text = stringResource(id = com.cadrikmdev.permissions.presentation.R.string.resolve),
                        isLoading = false
                    ) {
                        onAction(TrackOverviewAction.OnResolveLocationService)
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.unavailable),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = state.tracks,
                key = {
                    it.id
                }
            ) {
                TrackListItem(
                    trackUi = it,
                    onDeleteClick = {
                        onAction(TrackOverviewAction.DeleteTrack(it))
                    },
                    modifier = Modifier
                        .animateItemPlacement()
                )
            }
        }
    }
}

@Preview
@Composable
private fun TrackOverviewScreenPreview() {
    SignalTrackerTheme {
        TrackOverviewScreen(
            state = TrackOverviewState(
                isPermissionRequired = true,
                isLocationServiceEnabled = false,
                isLocationServiceResolvable = true,
            ),
            onAction = {},
            onEvent = {}
        )
    }
}
