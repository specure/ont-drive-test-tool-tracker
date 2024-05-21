@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.cadrikmdev.track.presentation.track_overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cadrikmdev.core.presentation.designsystem.LogoIcon
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.TrackIcon
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerFloatingActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.track.presentation.R
import com.cadrikmdev.track.presentation.track_overview.components.TrackListItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackOverviewScreenRoot(
    onStartTrackClick: () -> Unit,
    viewModel: TrackOverviewViewModel = koinViewModel(),
) {
    TrackOverviewScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                TrackOverviewAction.OnStartClick -> onStartTrackClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )
}

@Composable
private fun TrackOverviewScreen(
    state: TrackOverviewState,
    onAction: (TrackOverviewAction) -> Unit
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = topAppBarState
    )
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
            state = TrackOverviewState(),
            onAction = {}
        )
    }
}
