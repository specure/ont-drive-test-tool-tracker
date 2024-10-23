package com.specure.track.presentation.mappers

import androidx.compose.runtime.Composable
import com.specure.core.presentation.ui.UiText
import com.specure.track.presentation.R
import com.specure.updater.domain.UpdatingStatus

@Composable
fun UpdatingStatus.toUiString(): UiText {
    return when (this) {
        UpdatingStatus.Checking -> UiText.StringResource(R.string.checking)
        is UpdatingStatus.ErrorCheckingUpdate -> UiText.StringResource(R.string.checking_failed)
        is UpdatingStatus.ErrorDownloading -> UiText.StringResource(R.string.download_failed)
        UpdatingStatus.Downloading -> UiText.StringResource(R.string.downloading)
        is UpdatingStatus.Error -> UiText.StringResource(R.string.error)
        UpdatingStatus.Idle -> UiText.StringResource(R.string.idle)
        UpdatingStatus.InstallingSilently,
        UpdatingStatus.InstallingInteractive -> UiText.StringResource(R.string.installing)
        is UpdatingStatus.NewVersionFound -> UiText.StringResource(
            R.string.new_version_found,
            arrayOf(this.version)
        )

        UpdatingStatus.NoNewVersion -> UiText.StringResource(R.string.no_new_version)
    }
}