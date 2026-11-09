package com.specure.track.presentation.screen.settings

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
//    val isPingTestRunning: Boolean = false,
//    val currentPingMsValue: Long? = null,
//    val currentPingRunnerInfoRaw: String? = null,
//    val currentPingRunnerState: PingRunnerState? = null,
)