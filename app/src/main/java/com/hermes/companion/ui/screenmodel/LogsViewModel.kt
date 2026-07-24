package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel
import com.hermes.companion.ui.screen.LogsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LogsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
    }

    private fun loadLogs() {
        _uiState.update {
            it.copy(
                entries = listOf(
                    LogEntry("l1", LogLevel.INFO, System.currentTimeMillis() - 300000, "System", "Hermes Companion app started"),
                    LogEntry("l2", LogLevel.INFO, System.currentTimeMillis() - 280000, "Brain", "Connecting to brain at ws://192.168.1.100:8765"),
                    LogEntry("l3", LogLevel.INFO, System.currentTimeMillis() - 270000, "Brain", "WebSocket connected successfully"),
                    LogEntry("l4", LogLevel.DEBUG, System.currentTimeMillis() - 250000, "Agent", "Planner agent initialized"),
                    LogEntry("l5", LogLevel.DEBUG, System.currentTimeMillis() - 245000, "Agent", "Reasoner agent initialized"),
                    LogEntry("l6", LogLevel.DEBUG, System.currentTimeMillis() - 240000, "Agent", "Browser agent initialized"),
                    LogEntry("l7", LogLevel.INFO, System.currentTimeMillis() - 220000, "Mission", "Mission m1 started: Research AI Safety"),
                    LogEntry("l8", LogLevel.WARNING, System.currentTimeMillis() - 180000, "Agent", "Browser agent response slow: 2.4s latency"),
                    LogEntry("l9", LogLevel.INFO, System.currentTimeMillis() - 160000, "Memory", "Short-term memory consolidated (5 items)"),
                    LogEntry("l10", LogLevel.ERROR, System.currentTimeMillis() - 120000, "Agent", "Vision agent timeout after 30s"),
                ),
                isLoading = false
            )
        }
    }

    fun filterLevel(level: LogLevel?) {
        _uiState.update { it.copy(filterLevel = level) }
    }

    fun setAutoScroll(enabled: Boolean) {
        _uiState.update { it.copy(autoScroll = enabled) }
    }

    fun exportLogs() {
        // Export implementation
    }

    fun refresh() {
        loadLogs()
    }
}
