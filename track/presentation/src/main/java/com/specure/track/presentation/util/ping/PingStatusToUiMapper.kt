package com.specure.track.presentation.util.ping

import androidx.compose.runtime.Composable
import com.specure.core.presentation.ui.UiText
import com.specure.track.domain.ping.PingRunnerStatus
import com.specure.track.presentation.R

@Composable
fun PingRunnerStatus.toUIString(): String {
    return when (this) {
        PingRunnerStatus.Idle -> {
            UiText.StringResource(R.string.idle).asString()
        }

        is PingRunnerStatus.Error -> {
            UiText.StringResource(R.string.error).asString() + " " + this.exception.message
        }

        PingRunnerStatus.Running -> {
            UiText.StringResource(R.string.running).asString()
        }
    }
}
