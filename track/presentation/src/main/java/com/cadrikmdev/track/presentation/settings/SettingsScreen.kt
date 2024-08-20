package com.cadrikmdev.track.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cadrikmdev.core.domain.config.Config
import com.cadrikmdev.core.presentation.AppConfig
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.track.presentation.R
import me.zhanghai.compose.preference.ProvidePreferenceLocals
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
    viewModel: SettingsScreenViewModel = koinViewModel(),
) {
    SettingsScreen(
        onBackClick,
        viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsScreenViewModel
) {
    SignalTrackerTheme {
        SignalTrackerScaffold(
            topAppBar = {
                SignalTrackerToolbar(
                    showBackButton = true,
                    title = stringResource(id = R.string.settings),
                    onBackClick = onBackClick
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
                }
            }
        }

    }
}