package com.cadrikmdev.track.presentation.settings

import androidx.lifecycle.ViewModel
import com.cadrikmdev.core.database.export.DatabaseExporter
import com.cadrikmdev.core.domain.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    val appConfig: Config,
    private val stateManager: SettingsScreenStateManager,
    private val applicationScope: CoroutineScope,
    private val databaseExporter: DatabaseExporter,
) : ViewModel() {

    val stateFlow
        get() = this.stateManager.stateFlow

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnDatabaseClearClick -> TODO()
            SettingsAction.OnDatabaseClearExportedClick -> TODO()
            SettingsAction.OnDatabaseExportClick -> {
                this.applicationScope.launch {
                    databaseExporter.exportStateFlow.collect { exportState ->
                        when (exportState) {
                            is DatabaseExporter.ExportState.Initial -> {
                                stateManager.clearDatabaseExportState()
                            }

                            is DatabaseExporter.ExportState.Exporting -> {
                                stateManager.inProgressDatabaseExportState()
                            }

                            is DatabaseExporter.ExportState.Success -> {
                                stateManager.setDatabaseExportFinishedSuccessfully()
                            }

                            is DatabaseExporter.ExportState.NothingToExport -> {
                                stateManager.clearDatabaseExportState()
                            }

                            is DatabaseExporter.ExportState.Error -> {
                                stateManager.setDatabaseExportError()
                            }
                        }
                    }
                }

                applicationScope.launch {
                    databaseExporter.exportDatabase()
                }
            }
        }
    }

}