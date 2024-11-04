package com.specure.intercom.presentation.mappers

import androidx.compose.runtime.Composable
import com.specure.core.presentation.ui.UiText
import com.specure.intercom.domain.data.MeasurementState
import com.specure.intercom.presentation.R

@Composable
fun MeasurementState.toUiString(): String {
    return when (this) {
        MeasurementState.UNKNOWN -> UiText.StringResource(R.string.unknown).asString()
        MeasurementState.NOT_ACTIVATED -> UiText.StringResource(R.string.not_activated).asString()
        MeasurementState.IDLE -> UiText.StringResource(R.string.idle).asString()
        MeasurementState.STARTED -> UiText.StringResource(R.string.started).asString()
        MeasurementState.RUNNING -> UiText.StringResource(R.string.running).asString()
        MeasurementState.ERROR -> UiText.StringResource(R.string.error).asString()
        else -> this.toString()
    }
}