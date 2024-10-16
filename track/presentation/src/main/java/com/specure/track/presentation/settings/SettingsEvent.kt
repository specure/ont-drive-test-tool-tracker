package com.specure.track.presentation.settings

sealed interface SettingsEvent {
    data object OnDestroyed : SettingsEvent
}