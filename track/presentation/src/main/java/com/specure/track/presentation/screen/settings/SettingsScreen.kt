package com.specure.track.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.specure.core.domain.config.Config
import com.specure.core.presentation.AppConfig
import com.specure.core.presentation.designsystem.ArrowDownIcon
import com.specure.core.presentation.designsystem.ArrowUpIcon
import com.specure.core.presentation.designsystem.PingIcon
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import com.specure.core.presentation.designsystem.components.AppDialog
import com.specure.core.presentation.designsystem.components.SignalTrackerActionButton
import com.specure.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.specure.core.presentation.designsystem.components.SignalTrackerScaffold
import com.specure.core.presentation.designsystem.components.SignalTrackerToolbar
import com.specure.core.presentation.ui.UiText
import com.specure.track.presentation.R
import com.specure.track.presentation.util.ping.PingAction
import com.specure.track.presentation.util.ping.PingViewModel
import com.specure.track.presentation.util.ping.toUIString
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.sliderPreference
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.textFieldPreference
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import kotlin.math.round

@Composable
fun SettingsScreenRoot(
    onBackClick: () -> Unit,
    onOpenRadioSettingsClick: () -> Unit,
    settingsViewModel: SettingsScreenViewModel = koinViewModel(),
    pingViewModel: PingViewModel = koinViewModel()
) {
    SettingsScreen(
        onAction = { action ->
            when (action) {
                SettingsAction.OnOpenRadioSettingsClick -> onOpenRadioSettingsClick()
                SettingsAction.OnBackClick -> onBackClick()
                else -> {
                    settingsViewModel.onAction(action)
                }
            }
        },
        onEvent = { event ->
            settingsViewModel.onEvent(event)
        },
        settingsViewModel,
        onPingAction = { action ->
            when (action) {
                is PingAction.OnPingTestStartClick -> pingViewModel.startPing(action.host, action.timeoutMs)
                PingAction.OnPingTestStopClick -> pingViewModel.stopPing()
            }
        },
        pingViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAction: (SettingsAction) -> Unit,
    onEvent: (SettingsEvent) -> Unit,
    settingsViewModel: SettingsScreenViewModel,
    onPingAction: (PingAction) -> Unit,
    pingViewModel: PingViewModel,
) {

    LifecycleAwareComposable(
        onPause = {
            onEvent(SettingsEvent.OnDestroyed)
        }
    )

    val settingsState = settingsViewModel.stateFlow.collectAsStateWithLifecycle()
    val pingState = pingViewModel.uiState.collectAsStateWithLifecycle()

    SignalTrackerTheme {
        SignalTrackerScaffold(
            topAppBar = {
                SignalTrackerToolbar(
                    showBackButton = true,
                    title = stringResource(id = R.string.settings),
                    onBackClick = { onAction(SettingsAction.OnBackClick) }
                )
            },
        ) { padding ->
            ProvidePreferenceLocals {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    preferenceCategory(
                        key = "device_radio_settings_category",
                        title = { Text(text = stringResource(id = R.string.device_radio_settings)) },
                    )
                    preference(
                        key = "device_radio_settings",
                        title = { Text(text = stringResource(id = R.string.open_device_radio_settings)) },
                        onClick = { onAction(SettingsAction.OnOpenRadioSettingsClick) }
                    )

                    preferenceCategory(
                        key = "tracking_log_category",
                        title = { Text(text = stringResource(id = R.string.tracking_log)) },
                    )
                    val trackingLogMaxIntervalSeconds = 10F
                    val trackingLogIntervalSecondsStep = 1
                    val steps =
                        (trackingLogMaxIntervalSeconds.toInt() / trackingLogIntervalSecondsStep - 2)
                    println("Saving interval steps $steps")
                    sliderPreference(
                        key = Config.TRACKING_LOG_INTERVAL_SEC_CONFIG_KEY,
                        defaultValue = settingsViewModel.appConfig.getTrackingLogIntervalSecondsDefault()
                            .toFloat(),
                        valueRange = 1f..trackingLogMaxIntervalSeconds,
                        valueSteps = steps,
                        valueText = {
                            Text(
                                text = round(it).toInt().toString()
                            )
                        },
                        title = { Text(text = stringResource(id = R.string.tracking_log_interval_seconds)) },
                    )
                    if (AppConfig.FEATURE_PING_TEST_ENABLED) {
                        preferenceCategory(
                            key = "ping_test_category",
                            title = { Text(text = stringResource(id = R.string.ping_test)) },
                        )
                        switchPreference(
                            key = Config.PING_TEST_ENABLED_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getIsPingTestEnabledDefault(),
                            title = { Text(text = stringResource(id = R.string.ping_test)) },
                            summary = {
                                Text(
                                    text = if (it) stringResource(id = R.string.enabled) else stringResource(
                                        id = R.string.disabled
                                    )
                                )
                            },
                        )
                        textFieldPreference(
                            key = Config.PING_TEST_HOST_ADDRESS_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getPingTestHostAddressDefault(),
                            title = { Text(text = stringResource(id = R.string.ping_host_address)) },
                            textToValue = { it },
                            summary = { Text(text = it) }
                        )
                        val pingTimeoutMillisecondsValue = settingsViewModel.appConfig.getPingTimeoutMillisecondsDefault()
                        textFieldPreference(
                            key = Config.PING_TEST_TIMEOUT_MILLISECONDS_CONFIG_KEY,
                            defaultValue = pingTimeoutMillisecondsValue,
                            title = { Text(text = stringResource(id = R.string.timeout_milliseconds)) },
                            textToValue = {
                                try {
                                    val value = it.toInt()
                                    if (value > 0) {
                                        value
                                    } else {
                                        pingTimeoutMillisecondsValue
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e.localizedMessage)
                                    pingTimeoutMillisecondsValue
                                }
                            },
                            summary = { Text(text = it.toString()) }
                        )
                        item {
                            Row {
                                SignalTrackerOutlinedActionButton(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .weight(1f),
                                    text = if (pingState.value.isPingRunning())
                                        stringResource(id = com.specure.permissions.presentation.R.string.stop)
                                    else
                                        stringResource(id = com.specure.permissions.presentation.R.string.test_ping),
                                    isLoading = false
                                ) {
                                    if (pingState.value.isPingRunning()) {
                                        onPingAction(PingAction.OnPingTestStopClick)
                                    } else {
                                        onPingAction(
                                            PingAction.OnPingTestStartClick(
                                                settingsViewModel.appConfig.getPingTestHostAddress()
                                                    ?: settingsViewModel.appConfig.getPingTestHostAddressDefault(),
                                                (settingsViewModel.appConfig.getPingTimeoutMilliseconds()
                                                    ?: settingsViewModel.appConfig.getPingTimeoutMillisecondsDefault()).toLong()
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val pingValue = pingState.value.pingStats?.result?.ms ?: 0

                                pingValue.also {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(
                                            imageVector = PingIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Box(
                                            modifier = Modifier.alignByBaseline()
                                        ) {
                                            Text(
                                                text = it.toString(),
                                                style = MaterialTheme.typography.headlineSmall
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.alignByBaseline()
                                        ) {
                                            Text(
                                                text = UiText.StringResource(R.string.miliseconds_unit).asString(),
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        pingState.value.pingRunnerStatus?.let {
                                            Text(
                                                text = it.toUIString(),
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (AppConfig.FEATURE_SPEED_TEST_ENABLED) {
                        preferenceCategory(
                            key = "speed_test_category",
                            title = { Text(text = stringResource(id = R.string.speed_test)) },
                        )
                        switchPreference(
                            key = Config.SPEED_TEST_ENABLED_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getIsSpeedTestEnabledDefault(),
                            title = { Text(text = stringResource(id = R.string.speed_test)) },
                            summary = {
                                Text(
                                    text = if (it) stringResource(id = R.string.enabled) else stringResource(
                                        id = R.string.disabled
                                    )
                                )
                            },
                        )

                        textFieldPreference(
                            key = Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getSpeedTestDurationSecondsDefault(),
                            title = { Text(text = stringResource(id = R.string.duration_seconds)) },
                            textToValue = {
                                try {
                                    val value = it.toInt()
                                    if (value > 0) {
                                        value
                                    } else {
                                        settingsViewModel.appConfig.getSpeedTestDurationSecondsDefault()
                                    }
                                } catch (e: Exception) {
                                    Timber.e(e.localizedMessage)
                                    settingsViewModel.appConfig.getDownloadSpeedTestServerPortDefault()
                                }
                            },
                            summary = { Text(text = it.toString()) }
                        )

                        preferenceCategory(
                            key = "download_test_category",
                            title = { Text(text = stringResource(id = R.string.download_test)) },
                        )
                        textFieldPreference(
                            key = Config.DOWNLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getDownloadSpeedTestServerAddressDefault(),
                            title = { Text(text = stringResource(id = R.string.server_url)) },
                            textToValue = { it },
                            summary = { Text(text = it) }
                        )
                        textFieldPreference(
                            key = Config.DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getDownloadSpeedTestServerPortDefault(),
                            title = { Text(text = stringResource(id = R.string.server_port)) },
                            textToValue = {
                                try {
                                    it.toInt()
                                } catch (e: Exception) {
                                    Timber.e(e.localizedMessage)
                                    settingsViewModel.appConfig.getDownloadSpeedTestServerPortDefault()
                                }
                            },
                            summary = { Text(text = it.toString()) }
                        )
                        val downloadMaxBandwidthBitsPerSecondValue = 100000000f
                        val downloadMaxBandwidthBitsPerSecondValueStep = 10000000
                        val megaBitsToBitsRatio = 1000000
                        sliderPreference(
                            key = Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getDownloadSpeedTestMaxBandwidthBitsPerSecondsDefault()
                                .toFloat(),
                            valueRange = 0f..downloadMaxBandwidthBitsPerSecondValue,
                            valueSteps = (downloadMaxBandwidthBitsPerSecondValue / downloadMaxBandwidthBitsPerSecondValueStep - 1).toInt(),
                            valueText = {
                                Text(
                                    text = round(it / megaBitsToBitsRatio).toInt().toString()
                                )
                            },
                            title = { Text(text = stringResource(id = R.string.max_bandwidth)) },
                            summary = { Text(text = stringResource(id = R.string.max_bandwidth_details)) }
                        )

                        item {
                            Row {
                                SignalTrackerOutlinedActionButton(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .weight(1f),
                                    text = if (settingsState.value.isIperfDownloadRunning)
                                        stringResource(id = com.specure.permissions.presentation.R.string.stop)
                                    else
                                        stringResource(id = com.specure.permissions.presentation.R.string.test_down),
                                    isLoading = false
                                ) {
                                    onAction(SettingsAction.OnDownloadTestClick)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                settingsState.value.currentIperfDownloadSpeed?.let {
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
                                                text = settingsState.value.currentIperfDownloadSpeedUnit.toString(),
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        settingsState.value.currentIperfDownloadInfoRaw?.let {
                                            Box(
                                                modifier = Modifier.alignByBaseline(),
                                            ) {
                                                Text(
                                                    text = it,
                                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        preferenceCategory(
                            key = "upload_test_category",
                            title = { Text(text = stringResource(id = R.string.upload_test)) },
                        )
                        textFieldPreference(
                            key = Config.UPLOAD_SPEED_TEST_SERVER_ADDRESS_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getUploadSpeedTestServerAddressDefault(),
                            title = { Text(text = stringResource(id = R.string.server_url)) },
                            textToValue = { it },
                            summary = { Text(text = it) }
                        )
                        textFieldPreference(
                            key = Config.UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getUploadSpeedTestServerPortDefault(),
                            title = { Text(text = stringResource(id = R.string.server_port)) },
                            textToValue = {
                                try {
                                    it.toInt()
                                } catch (e: Exception) {
                                    Timber.e(e.localizedMessage)
                                }
                            },
                            summary = { Text(text = it.toString()) },
                        )
                        val uploadMaxBandwidthBitsPerSecondValue = 10000000F
                        val uploadMaxBandwidthBitsPerSecondValueStep = 10000000
                        sliderPreference(
                            key = Config.UPLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
                            defaultValue = settingsViewModel.appConfig.getUploadSpeedTestMaxBandwidthBitsPerSecondDefault()
                                .toFloat(),
                            valueRange = 0f..uploadMaxBandwidthBitsPerSecondValue,
                            valueSteps = (uploadMaxBandwidthBitsPerSecondValue / uploadMaxBandwidthBitsPerSecondValueStep - 1).toInt(),
                            valueText = {
                                Text(
                                    text = round(it / megaBitsToBitsRatio).toInt().toString()
                                )
                            },
                            title = { Text(text = stringResource(id = R.string.max_bandwidth)) },
                            summary = { Text(text = stringResource(id = R.string.max_bandwidth_details)) }
                        )

                        item {
                            Row {
                                SignalTrackerOutlinedActionButton(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .weight(1f),
                                    text = if (settingsState.value.isIperfUploadRunning)
                                        stringResource(id = com.specure.permissions.presentation.R.string.stop)
                                    else
                                        stringResource(id = com.specure.permissions.presentation.R.string.test_up),
                                    isLoading = false
                                ) {
                                    onAction(SettingsAction.OnUploadTestClick)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                settingsState.value.currentIperfUploadSpeed?.let {
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
                                                text = settingsState.value.currentIperfUploadSpeedUnit.toString(),
                                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                                            )
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        settingsState.value.currentIperfUploadInfoRaw?.let {
                                            Box(
                                                modifier = Modifier.alignByBaseline(),
                                            ) {
                                                Text(
                                                    text = it,
                                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    preferenceCategory(
                        key = "database_category",
                        title = { Text(text = stringResource(id = R.string.database)) },
                    )
                    preference(
                        key = "export_database",
                        title = { Text(text = stringResource(id = R.string.export_database)) },
                        summary = {
                            if (settingsState.value.isExportingDatabase) {
                                Text(text = stringResource(id = R.string.exporting))
                            } else if (settingsState.value.isExportingDatabaseError) {
                                Text(text = stringResource(id = R.string.exporting_error))
                            } else if (settingsState.value.isExportingDatabaseDoneSuccessfully) {
                                Text(text = stringResource(id = R.string.successfully_exported))
                            }
                        },
                        onClick = { onAction(SettingsAction.OnDatabaseExportClick) }
                    )
                    preference(
                        key = "database_clear_exported",
                        title = { Text(text = stringResource(id = R.string.remove_exported_items_form_database)) },
                        onClick = { onAction(SettingsAction.OnDatabaseClearExportedClick) }
                    )

                    preference(
                        key = "database_clear",
                        title = { Text(text = stringResource(id = R.string.clean_whole_database)) },
                        onClick = {
                            onAction(SettingsAction.OnDatabaseClearClick)
                        }
                    )
                }

                if (settingsState.value.isClearDatabaseDialogShown) {
                    AppDialog(
                        title = stringResource(
                            id = R.string.want_clear_database
                        ),
                        onDismiss = {
                            onAction(SettingsAction.OnDatabaseClearCancelClick)
                        },
                        description = stringResource(id = R.string.data_will_be_lost),
                        primaryButton = {
                            SignalTrackerActionButton(
                                text = stringResource(id = R.string.cancel),
                                isLoading = false,
                                onClick = {
                                    onAction(SettingsAction.OnDatabaseClearCancelClick)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        },
                        secondaryButton = {
                            SignalTrackerOutlinedActionButton(
                                text = stringResource(id = R.string.confirm),
                                isLoading = false,
                                onClick = {
                                    onAction(SettingsAction.OnDatabaseClearConfirmClick)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LifecycleAwareComposable(
    lifecycleOwner: LifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current).value,
    onPause: () -> Unit,
) {
    val currentOnPause by rememberUpdatedState(onPause)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                currentOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}