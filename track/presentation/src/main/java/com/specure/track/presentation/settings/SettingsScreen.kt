package com.specure.track.presentation.settings

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
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import com.specure.core.presentation.designsystem.components.AppDialog
import com.specure.core.presentation.designsystem.components.SignalTrackerActionButton
import com.specure.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.specure.core.presentation.designsystem.components.SignalTrackerScaffold
import com.specure.core.presentation.designsystem.components.SignalTrackerToolbar
import com.specure.track.presentation.R
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
    viewModel: SettingsScreenViewModel = koinViewModel(),
) {
    SettingsScreen(
        onAction = { action ->
            when (action) {
                SettingsAction.OnOpenRadioSettingsClick -> onOpenRadioSettingsClick()
                SettingsAction.OnBackClick -> onBackClick()
                else -> {
                    viewModel.onAction(action)
                }
            }
        },
        onEvent = { event ->
            viewModel.onEvent(event)
        },
        viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onAction: (SettingsAction) -> Unit,
    onEvent: (SettingsEvent) -> Unit,
    viewModel: SettingsScreenViewModel
) {

    LifecycleAwareComposable(
        onPause = {
            onEvent(SettingsEvent.OnDestroyed)
        }
    )

    val state = viewModel.stateFlow.collectAsStateWithLifecycle()

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
                        defaultValue = viewModel.appConfig.getTrackingLogIntervalSecondsDefault()
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
                    if (AppConfig.FEATURE_SPEED_TEST_ENABLED) {
                        switchPreference(
                            key = Config.SPEED_TEST_ENABLED_CONFIG_KEY,
                            defaultValue = viewModel.appConfig.getIsSpeedTestEnabledDefault(),
                            title = { Text(text = stringResource(id = R.string.speed_test)) },
                            summary = {
                                Text(
                                    text = if (it) stringResource(id = R.string.enabled) else stringResource(
                                        id = R.string.disabled
                                    )
                                )
                            },
                        )
                    }

                    textFieldPreference(
                        key = Config.SPEED_TEST_DURATION_SECONDS_CONFIG_KEY,
                        defaultValue = viewModel.appConfig.getSpeedTestDurationSecondsDefault(),
                        title = { Text(text = stringResource(id = R.string.duration_seconds)) },
                        textToValue = {
                            try {
                                val value = it.toInt()
                                if (value > 0) {
                                    value
                                } else {
                                    viewModel.appConfig.getSpeedTestDurationSecondsDefault()
                                }
                            } catch (e: Exception) {
                                Timber.e(e.localizedMessage)
                                viewModel.appConfig.getDownloadSpeedTestServerPortDefault()
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
                        defaultValue = viewModel.appConfig.getDownloadSpeedTestServerAddressDefault(),
                        title = { Text(text = stringResource(id = R.string.server_url)) },
                        textToValue = { it },
                        summary = { Text(text = it) }
                    )
                    textFieldPreference(
                        key = Config.DOWNLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
                        defaultValue = viewModel.appConfig.getDownloadSpeedTestServerPortDefault(),
                        title = { Text(text = stringResource(id = R.string.server_port)) },
                        textToValue = {
                            try {
                                it.toInt()
                            } catch (e: Exception) {
                                Timber.e(e.localizedMessage)
                                viewModel.appConfig.getDownloadSpeedTestServerPortDefault()
                            }
                        },
                        summary = { Text(text = it.toString()) }
                    )
                    val downloadMaxBandwidthBitsPerSecondValue = 100000000f
                    val downloadMaxBandwidthBitsPerSecondValueStep = 10000000
                    val megaBitsToBitsRatio = 1000000
                    sliderPreference(
                        key = Config.DOWNLOAD_SPEED_TEST_MAX_BANDWIDTH_BITS_PER_SEC_CONFIG_KEY,
                        defaultValue = viewModel.appConfig.getDownloadSpeedTestMaxBandwidthBitsPerSecondsDefault()
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
                                text = if (state.value.isIperfDownloadRunning)
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

                            state.value.currentIperfDownloadSpeed?.let {
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
                                            text = state.value.currentIperfDownloadSpeedUnit.toString(),
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    state.value.currentIperfDownloadInfoRaw?.let {
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
                        defaultValue = viewModel.appConfig.getUploadSpeedTestServerAddressDefault(),
                        title = { Text(text = stringResource(id = R.string.server_url)) },
                        textToValue = { it },
                        summary = { Text(text = it) }
                    )
                    textFieldPreference(
                        key = Config.UPLOAD_SPEED_TEST_SERVER_PORT_CONFIG_KEY,
                        defaultValue = viewModel.appConfig.getUploadSpeedTestServerPortDefault(),
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
                        defaultValue = viewModel.appConfig.getUploadSpeedTestMaxBandwidthBitsPerSecondDefault()
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
                                text = if (state.value.isIperfUploadRunning)
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
                            state.value.currentIperfUploadSpeed?.let {
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
                                            text = state.value.currentIperfUploadSpeedUnit.toString(),
                                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    state.value.currentIperfUploadInfoRaw?.let {
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
                        key = "database_category",
                        title = { Text(text = stringResource(id = R.string.database)) },
                    )
                    preference(
                        key = "export_database",
                        title = { Text(text = stringResource(id = R.string.export_database)) },
                        summary = {
                            if (state.value.isExportingDatabase) {
                                Text(text = stringResource(id = R.string.exporting))
                            } else if (state.value.isExportingDatabaseError) {
                                Text(text = stringResource(id = R.string.exporting_error))
                            } else if (state.value.isExportingDatabaseDoneSuccessfully) {
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

                if (state.value.isClearDatabaseDialogShown) {
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