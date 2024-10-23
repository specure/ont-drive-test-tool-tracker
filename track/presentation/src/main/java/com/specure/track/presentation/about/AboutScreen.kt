@file:OptIn(ExperimentalMaterial3Api::class)

package com.specure.track.presentation.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.specure.core.presentation.designsystem.ArrowRightIcon
import com.specure.core.presentation.designsystem.SignalTrackerBlue
import com.specure.core.presentation.designsystem.SignalTrackerTheme
import com.specure.core.presentation.designsystem.components.SignalTrackerOutlinedActionButton
import com.specure.core.presentation.designsystem.components.SignalTrackerScaffold
import com.specure.core.presentation.designsystem.components.SignalTrackerToolbar
import com.specure.track.presentation.R
import com.specure.track.presentation.mappers.toUiString
import com.specure.track.presentation.track_overview.NetworkInfoRow
import com.specure.updater.domain.UpdatingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel

@Composable
fun AboutScreenRoot(
    onBackClick: () -> Unit,
    viewModel: AboutScreenViewModel = koinViewModel(),
) {
    AboutScreen(
        onBackClick,
        viewModel,
        updaterState = viewModel.updateState.collectAsState(),
        onAction = viewModel::onAction
    )
}

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    viewModel: AboutScreenViewModel,
    updaterState: State<UpdatingStatus>,
    onAction: (AboutScreenAction) -> Unit
) {
    AboutScreenContent(
        onBackClick = onBackClick,
        version = viewModel.versionName,
        updaterState = updaterState,
        onAction = onAction
    )
}

@Composable
fun AboutScreenContent(
    onBackClick: () -> Unit,
    version: String,
    updaterState: State<UpdatingStatus>,
    onAction: (AboutScreenAction) -> Unit,
    modifier: Modifier = Modifier
) {
    SignalTrackerTheme {
        SignalTrackerScaffold(
            topAppBar = {
                SignalTrackerToolbar(
                    showBackButton = true,
                    title = stringResource(id = R.string.about),
                    onBackClick = onBackClick
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(padding)
            ) {
                HorizontalDivider()
                InfoItemUpdater(
                    updaterState,
                    { onAction(AboutScreenAction.OnCheckUpdateClick) },
                    { onAction(AboutScreenAction.OnInstallUpdateClick) }
                )
                HorizontalDivider()
                InfoItem(
                    title = stringResource(R.string.version),
                    text = version,
                )
                InfoItemWebLink(
                    title = stringResource(R.string.developed_by),
                    text = "Specure GmbH",
                    url = "https://specure.com"
                )
            }
        }

    }
}

@Composable
fun InfoItemUpdater(
    updaterState: State<UpdatingStatus>,
    actionCheck: () -> Unit,
    actionInstall: () -> Unit
) {
    SignalTrackerOutlinedActionButton(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        text = when (updaterState.value) {
            UpdatingStatus.Checking,
            UpdatingStatus.Downloading,
            is UpdatingStatus.Error,
            UpdatingStatus.Idle,
            is UpdatingStatus.ErrorCheckingUpdate,
            UpdatingStatus.NoNewVersion -> stringResource(id = R.string.check_for_updates)

            is UpdatingStatus.ErrorDownloading,
            UpdatingStatus.InstallingInteractive,
            UpdatingStatus.InstallingSilently,
            is UpdatingStatus.NewVersionFound -> stringResource(id = R.string.install_update)
        },
        isLoading = updaterState.value in listOf(
            UpdatingStatus.Downloading,
            UpdatingStatus.Checking,
            UpdatingStatus.InstallingSilently,
        )
    ) {
        when (updaterState.value) {
            UpdatingStatus.InstallingInteractive,
            UpdatingStatus.InstallingSilently,
            UpdatingStatus.Checking,
            UpdatingStatus.Downloading -> {
            }

            is UpdatingStatus.Error,
            UpdatingStatus.Idle,
            is UpdatingStatus.ErrorCheckingUpdate,
            UpdatingStatus.NoNewVersion -> actionCheck()

            is UpdatingStatus.ErrorDownloading,
            is UpdatingStatus.NewVersionFound -> actionInstall()
        }
    }
    if (updaterState.value != UpdatingStatus.Idle) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            NetworkInfoRow(
                title = stringResource(id = R.string.update_status),
                value = updaterState.value.toUiString().asString(),
            )
        }
    }
}

@Composable
fun InfoItemWebLink(title: String?, text: String?, url: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    ) {
        InfoItem(
            title,
            text, modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
        )

        Box(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 8.dp)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = ArrowRightIcon, contentDescription = null, tint = SignalTrackerBlue,
                modifier = Modifier
                    .size(35.dp)
            )
        }
    }
}

@Composable
fun InfoItem(title: String?, text: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .defaultMinSize(56.dp, 56.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@PreviewScreenSizes
@Composable
fun InfoScreenPreview() {
    AboutScreenContent(
        onBackClick = {},
        version = "1.1.1 - alpha",
        updaterState = MutableStateFlow<UpdatingStatus>(UpdatingStatus.Idle).collectAsState(),
        onAction = {}
    )
}