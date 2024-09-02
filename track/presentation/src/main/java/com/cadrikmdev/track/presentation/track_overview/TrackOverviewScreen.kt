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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.cadrikmdev.connectivity.domain.mobile.MobileNetworkInfo
import com.cadrikmdev.connectivity.domain.mobile.MobileNetworkType
import com.cadrikmdev.connectivity.domain.mobile.PrimaryDataSubscription
import com.cadrikmdev.connectivity.domain.wifi.WifiNetworkInfo
import com.cadrikmdev.core.domain.Temperature
import com.cadrikmdev.core.domain.location.Location
import com.cadrikmdev.core.domain.location.LocationTimestamp
import com.cadrikmdev.core.domain.location.LocationWithDetails
import com.cadrikmdev.core.presentation.designsystem.ArrowDownIcon
import com.cadrikmdev.core.presentation.designsystem.ArrowUpIcon
import com.cadrikmdev.core.presentation.designsystem.LogoIcon
import com.cadrikmdev.core.presentation.designsystem.SettingsIcon
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.TrackIcon
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerFloatingActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.core.presentation.designsystem.components.util.DropDownItem
import com.cadrikmdev.core.presentation.ui.toLocalTime
import com.cadrikmdev.track.presentation.R
import org.koin.androidx.compose.koinViewModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun TrackOverviewScreenRoot(
    onStartTrackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onResolvePermissionClick: () -> Unit,
    viewModel: TrackOverviewViewModel = koinViewModel(),
) {
    TrackOverviewScreen(
        state = viewModel.state,
        onAction = { action ->
            when (action) {
                TrackOverviewAction.OnStartClick -> onStartTrackClick()
                TrackOverviewAction.OnSettingsClick -> onSettingsClick()
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
                onEvent(TrackOverviewEvent.OnUpdateServiceStatus)
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
                menuItems = listOf(
                    DropDownItem(
                        icon = SettingsIcon,
                        title = stringResource(id = R.string.settings)
                    ),
                ),
                onMenuItemClick = { index ->
                    when (index) {
                        0 -> onAction(TrackOverviewAction.OnSettingsClick)
                    }
                },
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
                enabled = state.isPossibleToStartMeasurement(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.wifi_service_enabled))
                if (state.isWifiServiceEnabled)
                    Text(
                        text = stringResource(id = R.string.enabled),
                        color = MaterialTheme.colorScheme.error
                    )
                else
                    Text(
                        text = stringResource(id = R.string.disabled),
                    )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = stringResource(id = R.string.location_service))
                if (state.isLocationServiceEnabled) {
                    Text(
                        text = stringResource(id = R.string.enabled)
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
                        text = stringResource(id = R.string.disabled),
                        color = MaterialTheme.colorScheme.error
                    )
                }
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

            state.currentTemperatureCelsius?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(16.dp),
                        )
                ) {
                    NetworkInfoRow(
                        title = stringResource(id = R.string.temperature),
                        value = (it.temperatureCelsius ?: "-").toString() + " °C",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.updated_at),
                        value = (it.timestampMillis.toDuration(DurationUnit.MILLISECONDS)
                            .toLocalTime() ?: "-").toString(),
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                if (state.mobileNetworkInfo is MobileNetworkInfo) {
                    NetworkInfoRow(
                        title = stringResource(id = R.string.operator),
                        value = state.mobileNetworkInfo?.name.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.sim_count),
                        value = state.mobileNetworkInfo?.simCount.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.network_type),
                        value = state.mobileNetworkInfo?.networkType.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.primary_signal),
                        value = state.mobileNetworkInfo?.primarySignalDbm.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.updated_at),
                        value = state.mobileNetworkInfo?.timestampMillis?.toDuration(DurationUnit.MILLISECONDS)
                            ?.toLocalTime().toString() ?: "-",
                    )
                }
                if (state.mobileNetworkInfo is WifiNetworkInfo) {
                    NetworkInfoRow(
                        title = stringResource(id = R.string.network_type),
                        value = state.mobileNetworkInfo?.type.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.name),
                        value = state.mobileNetworkInfo?.name.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.primary_signal),
                        value = state.mobileNetworkInfo?.rssi.toString() ?: "-",
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.updated_at),
                        value = state.mobileNetworkInfo?.timestampMillis?.toDuration(DurationUnit.MILLISECONDS)
                            ?.toLocalTime().toString() ?: "-",
                    )
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                if (state.location != null) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.location_data_missing),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.location?.let { location ->
                    NetworkInfoRow(
                        title = stringResource(id = R.string.latitude),
                        value = location.location.location.lat.toString()
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.longitude),
                        value = location.location.location.long.toString()
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.updated_at),
                        value = location.location.timestamp.toLocalTime().toString()
                    )
                    NetworkInfoRow(
                        title = stringResource(id = R.string.provider),
                        value = location.location.source
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                NetworkInfoRow(
                    title = stringResource(id = R.string.tracks_count_for_export),
                    value = state.trackCountForExport.toString(),
                )
            }

            if (state.trackCountForExport > 0) {
                SignalTrackerOutlinedActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.export_to_csv),
                    isLoading = state.fileExport?.progress != null && state.fileExport.progress != 100
                ) {
                    onAction(TrackOverviewAction.OnExportToCsvClick)
                }
            }

            if (state.isSpeedTestEnabled) {
                Row {
                    SignalTrackerOutlinedActionButton(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        text = if (state.isIperfDownloadRunning)
                            stringResource(id = com.cadrikmdev.permissions.presentation.R.string.stop)
                        else
                            stringResource(id = com.cadrikmdev.permissions.presentation.R.string.test_down),
                        isLoading = false
                    ) {
                        onAction(TrackOverviewAction.OnDownloadTestClick)
                    }
                    Spacer(modifier = Modifier.weight(0.5f))
                    SignalTrackerOutlinedActionButton(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        text = if (state.isIperfUploadRunning)
                            stringResource(id = com.cadrikmdev.permissions.presentation.R.string.stop)
                        else
                            stringResource(id = com.cadrikmdev.permissions.presentation.R.string.test_up),
                        isLoading = false
                    ) {
                        onAction(TrackOverviewAction.OnUploadTestClick)
                    }
                }

                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

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
                                    text = state.currentIperfDownloadSpeedUnit.toString(),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
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
                                    text = state.currentIperfUploadSpeedUnit.toString(),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                )
                            }
                        }
                    }
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
        }
    }
}

@Composable
fun NetworkInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            color = MaterialTheme.colorScheme.onSurface
        )
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
                    primarySignalDbm = -120,
                    capabilitiesRaw = null,
                ),
                currentIperfDownloadInfoRaw = "fdsjf rlkt herukjfn ef uheirfu ef ernfhu fieru fheriuferiuheruih ",
                currentIperfDownloadSpeed = "20",
                currentIperfUploadSpeed = "2",
                currentTemperatureCelsius = Temperature(
                    22.3,
                    System.currentTimeMillis(),
                ),
                location = LocationTimestamp(
                    location = LocationWithDetails(
                        location = Location(4.94135, 4.8965),
                        source = "GPS",
                        altitude = 256.5,
                        age = 10.toDuration(DurationUnit.SECONDS),
                        timestamp = 1520.toDuration(DurationUnit.SECONDS)
                    ),
                    durationTimestamp = 123.toDuration(DurationUnit.SECONDS)
                )
            ),
            onAction = {},
            onEvent = {}
        )
    }
}
