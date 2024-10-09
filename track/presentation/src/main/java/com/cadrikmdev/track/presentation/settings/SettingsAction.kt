package com.cadrikmdev.track.presentation.settings

sealed interface SettingsAction {
    data object OnDatabaseExportClick : SettingsAction
    data object OnDatabaseClearClick : SettingsAction
    data object OnDatabaseClearExportedClick : SettingsAction
}