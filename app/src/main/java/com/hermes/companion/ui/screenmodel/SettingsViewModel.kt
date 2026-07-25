package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    fun updateHost(host: String) {
        _uiState.update { it.copy(brainHost = host) }
    }

    fun updatePort(port: String) {
        _uiState.update { it.copy(brainPort = port) }
    }

    fun updateAuthToken(token: String) {
        _uiState.update { it.copy(authToken = token) }
    }

    fun toggleNotifications() {
        _uiState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
    }

    fun toggleDarkTheme() {
        _uiState.update { it.copy(darkTheme = !it.darkTheme) }
    }

    fun toggleDataCollection() {
        _uiState.update { it.copy(dataCollection = !it.dataCollection) }
    }

    fun toggleCrashReporting() {
        _uiState.update { it.copy(crashReporting = !it.crashReporting) }
    }
}

data class SettingsUiState(
    val brainHost: String = "127.0.0.1",
    val brainPort: String = "9876",
    val authToken: String = "",
    val darkTheme: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dataCollection: Boolean = false,
    val crashReporting: Boolean = true
)
