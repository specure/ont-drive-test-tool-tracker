package com.cadrikmdev.track.presentation.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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
}