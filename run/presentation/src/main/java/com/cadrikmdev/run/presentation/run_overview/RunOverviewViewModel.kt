package com.cadrikmdev.run.presentation.run_overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadrikmdev.core.domain.run.RunRepository
import com.cadrikmdev.run.presentation.run_overview.mapper.toRunUi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RunOverviewViewModel(
    private val runRepository: RunRepository,
) : ViewModel() {

    var state by mutableStateOf(RunOverviewState())
        private set

    init {
        runRepository.getRuns().onEach { runs ->
            val runUis = runs.map { it.toRunUi() }
            state = state.copy(runs = runUis)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            runRepository.syncPendingRuns()
            runRepository.fetchRuns()
        }
    }

    fun onAction(action: RunOverviewAction) {
        when (action) {
            RunOverviewAction.OnLogoutClick -> Unit
            RunOverviewAction.OnStartClick -> Unit
            is RunOverviewAction.DeleteRun -> {
                viewModelScope.launch {
                    runRepository.deleteRun(action.runUi.id)
                }
            }

            else -> Unit
        }
    }
}