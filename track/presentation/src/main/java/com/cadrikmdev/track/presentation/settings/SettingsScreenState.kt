package com.cadrikmdev.track.presentation.settings

data class SettingsScreenState(
    val isExportingDatabase: Boolean = false,
    val isExportingDatabaseDoneSuccessfully: Boolean = false,
    val isExportingDatabaseError: Boolean = false,
    val isClearDatabaseDialogShown: Boolean = false,
)