package com.specure.track.presentation.screen.settings

sealed interface SettingsEvent {
    data object OnDestroyed : SettingsEvent
}