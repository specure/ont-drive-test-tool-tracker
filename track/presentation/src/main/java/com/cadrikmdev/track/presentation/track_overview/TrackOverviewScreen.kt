@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.cadrikmdev.track.presentation.track_overview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkInfo
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.MobileNetworkType
import com.cadrikmdev.core.connectivty.domain.connectivity.mobile.PrimaryDataSubscription
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.presentation.designsystem.ArrowDownIcon
import com.cadrikmdev.core.presentation.designsystem.ArrowLeftIcon
import com.cadrikmdev.core.presentation.designsystem.ArrowUpIcon
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
//                TrackOverviewAction.OnStartClick -> onStartTrackClick()
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
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
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
                .verticalScroll(rememberScrollState())
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                state.currentTemperatureCelsius?.let {
                    Row(
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = it.temperatureCelsius.toString(),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = "°C",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        }
                    }
                }

                state.currentIperfDownloadSpeed?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = ArrowDownIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = "MBit/s",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        }
                    }
                }


                state.currentIperfUploadSpeed?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = ArrowUpIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.alignByBaseline()
                        ) {
                            Text(
                                text = "MBit/s",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        }
                    }
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                Text(
                    text = state.mobileNetworkInfo?.name.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.simOperatorMccMnc.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.simCountryIso.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.networkType.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.isRoaming.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.obtainedTimestampMillis.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.isPrimaryDataSubscription.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = state.mobileNetworkInfo?.simCount.toString() ?: "-",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row {
                state.currentIperfDownloadInfoRaw?.let {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = it,
                        fontSize = 8.sp
                    )
                }
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                )
                state.currentIperfUploadInfoRaw?.let {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = it,
                        fontSize = 8.sp
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
                mobileNetworkInfo = MobileNetworkInfo(
                    name = "O2",
                    networkType = MobileNetworkType.LTE_CA,
                    simDisplayName = "SDName",
                    simOperatorName = "simOperName",
                    simOperatorMccMnc = "233-02",
                    simCountryIso = "US",
                    operatorName = "operName",
                    mcc = 233,
                    mnc = 2,
                    isRoaming = false,
                    isPrimaryDataSubscription = PrimaryDataSubscription.TRUE,
                    simCount = 1,
                    obtainedTimestampMillis = 194656515616
                ),
                currentIperfDownloadInfoRaw = "fdsjf rlkt herukjfn ef uheirfu ef ernfhu fieru fheriuferiuheruih ",
                currentIperfDownloadSpeed = "20",
                currentIperfUploadSpeed = "2",
                currentTemperatureCelsius = Temperature(
                    22.3f,
                    System.currentTimeMillis(),
                )

            ),
            onAction = {},
            onEvent = {}
        )
    }
}
