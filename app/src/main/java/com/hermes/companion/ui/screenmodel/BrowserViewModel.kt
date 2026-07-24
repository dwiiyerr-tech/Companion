package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        _uiState.update {
            it.copy(
                isCdpConnected = true,
                tabCount = 5,
                currentUrl = "https://github.com/nousresearch/hermes",
                isLoading = false,
                logEntries = listOf(
                    LogEntry("l1", LogLevel.INFO, System.currentTimeMillis(), "Browser", "Connected to CDP endpoint"),
                    LogEntry("l2", LogLevel.INFO, System.currentTimeMillis(), "Browser", "Page loaded: github.com"),
                    LogEntry("l3", LogLevel.DEBUG, System.currentTimeMillis(), "Network", "GET /api/repos → 200 OK"),
                    LogEntry("l4", LogLevel.WARNING, System.currentTimeMillis(), "Browser", "Slow network request detected: 3.2s"),
                    LogEntry("l5", LogLevel.DEBUG, System.currentTimeMillis(), "DOM", "MutationObserver: 2 nodes added"),
                    LogEntry("l6", LogLevel.INFO, System.currentTimeMillis(), "Browser", "Screenshot saved to /screenshots/page.png")
                )
            )
        }
    }

    fun refresh() {
        loadState()
    }
}

data class BrowserUiState(
    val isCdpConnected: Boolean = false,
    val tabCount: Int = 0,
    val currentUrl: String = "",
    val isLoading: Boolean = false,
    val logEntries: List<LogEntry> = emptyList()
)
