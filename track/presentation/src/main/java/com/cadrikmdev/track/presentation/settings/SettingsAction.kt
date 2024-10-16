package com.cadrikmdev.track.presentation.settings

sealed interface SettingsAction {
    data object OnDatabaseExportClick : SettingsAction
    data object OnDatabaseClearClick : SettingsAction
    data object OnDatabaseClearConfirmClick : SettingsAction
    data object OnDatabaseClearCancelClick : SettingsAction
    data object OnDatabaseClearExportedClick : SettingsAction
    data object OnOpenRadioSettingsClick : SettingsAction
    data object OnDownloadTestClick : SettingsAction
    data object OnUploadTestClick : SettingsAction
    data object OnBackClick : SettingsAction
}