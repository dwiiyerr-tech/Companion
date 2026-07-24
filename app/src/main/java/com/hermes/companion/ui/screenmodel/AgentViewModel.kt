package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// UI-specific agent display model (not domain Agent)
data class AgentCard(
    val type: AgentType,
    val name: String,
    val status: AgentStatus,
    val cpu: Float = 0f,
    val ram: Float = 0f,
    val currentTask: String? = null
)

enum class AgentType {
    SUPERVISOR, PLANNER, REASONER, ANDROID, BROWSER, VISION, VOICE, MEMORY, RESEARCH, SECURITY, AUTOMATION
}

enum class AgentStatus {
    ACTIVE, IDLE, ERROR, OFFLINE
}

data class AgentUiState(
    val agents: List<AgentCard> = emptyList(),
    val filterStatus: AgentStatus? = null
)

class AgentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        loadAgents()
    }

    private fun loadAgents() {
        _uiState.update {
            it.copy(
                agents = listOf(
                    AgentCard(AgentType.SUPERVISOR, "Hermes Supervisor", AgentStatus.ACTIVE, 12f, 28f, "Coordinating mission m1"),
                    AgentCard(AgentType.PLANNER, "Mission Planner", AgentStatus.ACTIVE, 8f, 22f, "Planning: AI Safety Research"),
                    AgentCard(AgentType.REASONER, "Deep Reasoner", AgentStatus.IDLE, 2f, 15f, null),
                    AgentCard(AgentType.ANDROID, "Device Controller", AgentStatus.ACTIVE, 18f, 35f, "Running UI test flow"),
                    AgentCard(AgentType.BROWSER, "Web Navigator", AgentStatus.ACTIVE, 25f, 42f, "Scraping competitor page"),
                    AgentCard(AgentType.VISION, "Screen Analyzer", AgentStatus.IDLE, 5f, 18f, null),
                    AgentCard(AgentType.VOICE, "Voice Interface", AgentStatus.OFFLINE, 0f, 0f, null),
                    AgentCard(AgentType.MEMORY, "Memory Manager", AgentStatus.ACTIVE, 10f, 30f, "Consolidating short-term items"),
                    AgentCard(AgentType.RESEARCH, "Research Analyst", AgentStatus.ACTIVE, 45f, 55f, "Querying arxiv papers"),
                    AgentCard(AgentType.SECURITY, "Security Monitor", AgentStatus.IDLE, 6f, 20f, null),
                    AgentCard(AgentType.AUTOMATION, "Task Automator", AgentStatus.ERROR, 30f, 60f, "Failed: API connection timeout")
                )
            )
        }
    }

    fun filterByStatus(status: AgentStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
    }

    fun refresh() {
        loadAgents()
    }
}
