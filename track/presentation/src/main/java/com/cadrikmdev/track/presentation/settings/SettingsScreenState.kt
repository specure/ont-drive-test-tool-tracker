package com.cadrikmdev.track.presentation.settings

data class SettingsScreenState(
    val isExportingDatabase: Boolean = false,
    val isExportingDatabaseDoneSuccessfully: Boolean = false,
    val isExportingDatabaseError: Boolean = false,
    val isClearDatabaseDialogShown: Boolean = false,
    val currentIperfDownloadSpeed: String? = null,
    val currentIperfDownloadSpeedUnit: String? = null,
    val currentIperfUploadSpeed: String? = null,
    val currentIperfUploadSpeedUnit: String? = null,
    val isIperfUploadRunning: Boolean = false,
    val isIperfDownloadRunning: Boolean = false,
    val currentIperfDownloadInfoRaw: String? = null,
    val currentIperfUploadInfoRaw: String? = null,
)