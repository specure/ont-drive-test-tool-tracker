package com.cadrikmdev.track.presentation.settings

sealed interface SettingsEvent {
    data object OnDestroyed : SettingsEvent
}