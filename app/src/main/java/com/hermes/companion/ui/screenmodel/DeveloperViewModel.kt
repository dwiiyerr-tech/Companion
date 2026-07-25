package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EventLevel(val color: androidx.compose.ui.graphics.Color) {
    DEBUG(com.hermes.companion.ui.theme.StatusBlue),
    INFO(com.hermes.companion.ui.theme.StatusGreen),
    WARN(com.hermes.companion.ui.theme.StatusYellow),
    ERROR(com.hermes.companion.ui.theme.StatusRed)
}

data class BusEvent(
    val id: String,
    val level: EventLevel,
    val source: String,
    val event: String,
    val data: String,
    val timestamp: Long,
    val isExpanded: Boolean = false
)

data class LogConsoleEntry(
    val id: String,
    val level: EventLevel,
    val message: String,
    val timestamp: Long
)

data class MemoryInfoData(
    val heapSize: String = "72",
    val allocated: String = "48",
    val freeMemory: String = "24",
    val gcCount: Int = 0,
    val lastGC: Long = 0L
)

data class DeveloperUiState(
    val outputLines: List<String> = listOf("Hermes Dev Mode v1.0", "Type 'help' for commands"),
    val consoleEntries: List<LogConsoleEntry> = emptyList(),
    val busEvents: List<BusEvent> = emptyList(),
    val memoryInfo: MemoryInfoData = MemoryInfoData(),
    val filterLevel: EventLevel? = null,
    val memoryInspectResult: String = "",
    val apiInspectResult: String = ""
)

class DeveloperViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<String>>(emptyList())
    val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        _uiState.value = _uiState.value.copy(
            consoleEntries = listOf(
                LogConsoleEntry("c1", EventLevel.INFO, "System initialized", 0),
                LogConsoleEntry("c2", EventLevel.DEBUG, "Agent planner ready", 5),
                LogConsoleEntry("c3", EventLevel.WARN, "Slow response from brain", 120),
                LogConsoleEntry("c4", EventLevel.ERROR, "Vision agent timeout", 300)
            ),
            busEvents = listOf(
                BusEvent("e1", EventLevel.INFO, "System", "connected", "WebSocket connected", 100, false),
                BusEvent("e2", EventLevel.DEBUG, "Brain", "heartbeat", "OK", 200, false),
                BusEvent("e3", EventLevel.WARN, "Agent", "latency", "2.4s", 350, false),
                BusEvent("e4", EventLevel.ERROR, "Vision", "timeout", "Agent timed out after 30s", 500, false)
            ),
            memoryInfo = MemoryInfoData(
                heapSize = "72",
                allocated = "48",
                freeMemory = "24",
                gcCount = 12,
                lastGC = 45000L
            )
        )
    }

    fun executeCommand(cmd: String) {
        _commandHistory.value = _commandHistory.value + cmd
        _uiState.value = _uiState.value.copy(
            outputLines = _uiState.value.outputLines + cmd + "OK: executed"
        )
    }

    fun clearLog() {
        _uiState.value = DeveloperUiState()
    }

    fun sendDebugCommand(command: String) {
        executeCommand(command)
    }

    fun filterLevel(level: EventLevel?) {
        _uiState.value = _uiState.value.copy(filterLevel = level)
    }

    fun toggleEventExpanded(eventId: String) {
        _uiState.value = _uiState.value.copy(
            busEvents = _uiState.value.busEvents.map { event ->
                if (event.id == eventId) event.copy(isExpanded = !event.isExpanded) else event
            }
        )
    }

    fun refresh() {
        loadMockData()
    }
}
