package com.specure.track.presentation.util.ping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specure.track.domain.ping.PingRunnerState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PingViewModel(
    private val pingManager: PingManager
) : ViewModel() {

    // Directly expose the manager's state flow to the UI layer
    val uiState: StateFlow<PingRunnerState> = pingManager.pingState

    fun startPing(host: String, timeoutMs: Long) {
        viewModelScope.launch {
            pingManager.startPing(host, timeoutMs, viewModelScope)
        }
    }

    fun sendSinglePing(host: String) {
        viewModelScope.launch {
            pingManager.sendSinglePing(host)
        }
    }

    fun stopPing() {
        pingManager.stopPing()
    }

    override fun onCleared() {
        super.onCleared()
        stopPing()
    }

    fun onAction(action: PingAction) {
        when (action) {
            is PingAction.OnPingTestStartClick -> startPing(action.host, action.timeoutMs)
            PingAction.OnPingTestStopClick -> stopPing()
        }
    }

}