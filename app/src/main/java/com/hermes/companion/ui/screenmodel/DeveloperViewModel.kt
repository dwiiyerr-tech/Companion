package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeveloperViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

    fun executeCommand(cmd: String) {
        _commandHistory.value = _commandHistory.value + cmd
        _uiState.value = _uiState.value.copy(outputLines = _uiState.value.outputLines + cmd + "OK: executed")
    }

    fun clearLog() {
        _uiState.value = DeveloperUiState()
    }

    fun sendDebugCommand(command: String) {
        executeCommand(command)
    }
}

data class DeveloperUiState(
    val outputLines: List<String> = listOf("Hermes Dev Mode v1.0", "Type 'help' for commands"),
    val eventStream: List<DevEvent> = emptyList(),
    val memoryInspectResult: String = "",
    val apiInspectResult: String = ""
)

data class DevEvent(
    val timestamp: String,
    val source: String,
    val data: String
)
