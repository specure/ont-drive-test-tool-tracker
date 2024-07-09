package com.cadrikmdev.track.presentation.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cadrikmdev.core.presentation.designsystem.SignalTrackerTheme
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerScaffold
import com.cadrikmdev.core.presentation.designsystem.components.SignalTrackerToolbar
import com.cadrikmdev.track.domain.config.Config
import com.cadrikmdev.track.presentation.R
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.switchPreference
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenRoot(
    onBackClick: () -> Unit,
    viewModel: SettingsScreenViewModel = koinViewModel(),
) {
    SettingsScreen(
        onBackClick

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
) {
    SignalTrackerTheme {
        SignalTrackerScaffold(
            topAppBar = {
                SignalTrackerToolbar(
                    showBackButton = true,
                    title = stringResource(id = R.string.setings),
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
                    switchPreference(
                        key = Config.SPEED_TEST_ENABLED_CONFIG_KEY,
                        defaultValue = false,
                        title = { Text(text = "Speed test") },
                        summary = { Text(text = if (it) "On" else "Off") }
                    )
                }
            }
        }

    }
}