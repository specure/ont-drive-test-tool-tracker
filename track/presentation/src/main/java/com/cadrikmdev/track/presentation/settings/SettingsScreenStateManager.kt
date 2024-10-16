package com.cadrikmdev.track.presentation.settings

import com.cadrikmdev.iperf.domain.IperfTest
import com.cadrikmdev.iperf.domain.IperfTestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class SettingsScreenStateManager {

    private val viewModelState: MutableStateFlow<SettingsScreenState> by lazy {
        MutableStateFlow(
            this.setInitialState()
        )
    }
    val stateFlow = this.viewModelState

    val state
        get() = stateFlow.value

    fun setInitialState(): SettingsScreenState {
        return SettingsScreenState(
            isExportingDatabase = false,
            isExportingDatabaseError = false,
            isExportingDatabaseDoneSuccessfully = false,
            isClearDatabaseDialogShown = false,
        )
    }

    fun clearDatabaseExportState() {
        this.viewModelState.update { state ->
            state.copy(
                isExportingDatabase = false,
                isExportingDatabaseError = false,
                isExportingDatabaseDoneSuccessfully = false
            )
        }
    }

    fun inProgressDatabaseExportState() {
        this.viewModelState.update { state ->
            state.copy(
                isExportingDatabase = true,
                isExportingDatabaseError = false,
                isExportingDatabaseDoneSuccessfully = false
            )
        }
    }

    fun setDatabaseExportError() {
        this.viewModelState.update { state ->
            state.copy(
                isExportingDatabase = false,
                isExportingDatabaseError = true,
                isExportingDatabaseDoneSuccessfully = false
            )
        }
    }

    fun setDatabaseExportFinishedSuccessfully() {
        this.viewModelState.update { state ->
            state.copy(
                isExportingDatabase = false,
                isExportingDatabaseError = false,
                isExportingDatabaseDoneSuccessfully = true
            )
        }
    }

    fun showClearDatabaseDialog() {
        this.viewModelState.update { state ->
            state.copy(
                isClearDatabaseDialogShown = true
            )
        }
    }

    fun hideClearDatabaseDialog() {
        this.viewModelState.update { state ->
            state.copy(
                isClearDatabaseDialogShown = false
            )
        }
    }

    fun setIperfDownloadProgress(test: IperfTest) {
        this.viewModelState.update { state ->
            Timber.d("STATUS: ${test.status}")
            if (test.testProgress.isEmpty()) {
                state.copy(
                    currentIperfDownloadInfoRaw = test.status.toString(),
                    currentIperfDownloadSpeed = "-",
                    currentIperfDownloadSpeedUnit = "",
                    isIperfDownloadRunning = test.status in setOf(
                        IperfTestStatus.RUNNING,
                        IperfTestStatus.INITIALIZING
                    )
                )
            } else {
                state.copy(
                    currentIperfDownloadInfoRaw = test.status.toString(),
                    currentIperfDownloadSpeed = test.testProgress.last().bandwidth.toString(),
                    currentIperfDownloadSpeedUnit = test.testProgress.last().bandwidthUnit,
                    isIperfDownloadRunning = test.status in setOf(
                        IperfTestStatus.RUNNING,
                        IperfTestStatus.INITIALIZING
                    )
                )
            }

        }
    }

    fun setIperfUploadProgress(test: IperfTest) {
        this.viewModelState.update { state ->
            if (test.testProgress.isEmpty()) {
                state.copy(
                    currentIperfUploadInfoRaw = test.status.toString(),
                    currentIperfUploadSpeed = "-",
                    currentIperfUploadSpeedUnit = "",
                    isIperfUploadRunning = test.status in setOf(
                        IperfTestStatus.RUNNING,
                        IperfTestStatus.INITIALIZING
                    )
                )
            } else {
                state.copy(
                    currentIperfUploadInfoRaw = test.status.toString(),
                    currentIperfUploadSpeed = test.testProgress.last().bandwidth.toString(),
                    currentIperfUploadSpeedUnit = test.testProgress.last().bandwidthUnit,
                    isIperfUploadRunning = test.status in setOf(
                        IperfTestStatus.RUNNING,
                        IperfTestStatus.INITIALIZING
                    )
                )
            }

        }
    }

    fun isIperfDownloadRunning(): Boolean {
        return state.isIperfDownloadRunning
    }

    fun isIperfUploadRunning(): Boolean {
        return state.isIperfUploadRunning
    }
}