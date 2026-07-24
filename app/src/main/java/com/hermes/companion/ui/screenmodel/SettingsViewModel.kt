package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun saveConnection(host: String, port: String, token: String) {
        _uiState.value = _uiState.value.copy(
            brainHost = host, brainPort = port, authToken = token
        )
    }

    fun loadSettings() {
        // Load from DataStore
    }
}

data class SettingsUiState(
    val brainHost: String = "127.0.0.1",
    val brainPort: String = "9876",
    val authToken: String = "",
    val darkMode: Boolean = true,
    val notificationsEnabled: Boolean = true
)
