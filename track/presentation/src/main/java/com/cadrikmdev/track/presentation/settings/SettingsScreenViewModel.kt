package com.cadrikmdev.track.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.database.export.DatabaseManager
import com.cadrikmdev.core.domain.config.Config
import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.iperf.presentation.IperfDownloadRunner
import com.cadrikmdev.iperf.presentation.IperfUploadRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    val appConfig: Config,
    private val stateManager: SettingsScreenStateManager,
    private val applicationScope: CoroutineScope,
    private val databaseManager: DatabaseManager,
    private val iperfParser: IperfOutputParser,
    private val applicationContext: Context,
) : ViewModel() {

    val stateFlow
        get() = this.stateManager.stateFlow

    private val iperfUpload =
        IperfUploadRunner(applicationContext, applicationScope, iperfParser, appConfig)
    private val iperfDownload =
        IperfDownloadRunner(applicationContext, applicationScope, iperfParser, appConfig)

    init {
        iperfUpload.testProgressDetailsFlow.onEach {
            stateManager.setIperfUploadProgress(it)

        }.launchIn(viewModelScope)

        iperfDownload.testProgressDetailsFlow.onEach {
            stateManager.setIperfDownloadProgress(it)
        }.launchIn(viewModelScope)
    }

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

            SettingsAction.OnDownloadTestClick -> {
                viewModelScope.launch {
                    if (stateManager.isIperfDownloadRunning()) {
                        iperfDownload.stopTest()
                    } else {
                        iperfDownload.startTest()
                    }
                }
            }

            SettingsAction.OnUploadTestClick -> {
                viewModelScope.launch {
                    if (stateManager.isIperfUploadRunning()) {
                        iperfUpload.stopTest()
                    } else {
                        iperfUpload.startTest()
                    }
                }
            }

            SettingsAction.OnBackClick,
            SettingsAction.OnOpenRadioSettingsClick -> Unit
        }
    }

    private fun stopIperf() {
        iperfDownload.stopTest()
        iperfUpload.stopTest()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnDestroyed -> {
                stopIperf()
            }
        }
    }

}