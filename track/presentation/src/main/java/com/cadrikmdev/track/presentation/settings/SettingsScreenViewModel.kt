package com.cadrikmdev.track.presentation.settings

import androidx.lifecycle.ViewModel
import com.cadrikmdev.core.database.export.DatabaseManager
import com.cadrikmdev.core.domain.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    val appConfig: Config,
    private val stateManager: SettingsScreenStateManager,
    private val applicationScope: CoroutineScope,
    private val databaseManager: DatabaseManager,
) : ViewModel() {

    val stateFlow
        get() = this.stateManager.stateFlow

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnDatabaseClearClick -> {
                stateManager.showClearDatabaseDialog()
            }

            SettingsAction.OnDatabaseClearExportedClick -> {
                this.applicationScope.launch {
                    databaseManager.clearExportedItems()
                }
            }
            SettingsAction.OnDatabaseExportClick -> {
                this.applicationScope.launch {
                    databaseManager.exportStateFlow.collect { exportState ->
                        when (exportState) {
                            is DatabaseManager.ExportState.Initial -> {
                                stateManager.clearDatabaseExportState()
                            }

                            is DatabaseManager.ExportState.Exporting -> {
                                stateManager.inProgressDatabaseExportState()
                            }

                            is DatabaseManager.ExportState.Success -> {
                                stateManager.setDatabaseExportFinishedSuccessfully()
                            }

                            is DatabaseManager.ExportState.NothingToExport -> {
                                stateManager.clearDatabaseExportState()
                            }

                            is DatabaseManager.ExportState.Error -> {
                                stateManager.setDatabaseExportError()
                            }
                        }
                    }
                }

                applicationScope.launch {
                    databaseManager.exportDatabase()
                }
            }

            SettingsAction.OnDatabaseClearCancelClick -> {
                stateManager.hideClearDatabaseDialog()
            }

            SettingsAction.OnDatabaseClearConfirmClick -> {
                applicationScope.launch {
                    databaseManager.clearAllItems()
                }
                stateManager.hideClearDatabaseDialog()
            }

            SettingsAction.OnOpenRadioSettingsClick -> Unit
        }
    }

}