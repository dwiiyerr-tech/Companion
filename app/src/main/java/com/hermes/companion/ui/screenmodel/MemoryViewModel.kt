package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MemoryUiState(
    val memories: List<MemoryEntry> = emptyList(),
    val selectedTab: Int = 0,
    val searchQuery: String = ""
)

data class MemoryEntry(
    val id: String,
    val type: String,
    val content: String,
    val timestamp: String,
    val relevance: Float = 0f
)

class MemoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    var showAddDialog: Boolean = false

    init {
        loadMemories()
    }

    private fun loadMemories() {
        _uiState.update {
            it.copy(
                memories = listOf(
                    MemoryEntry("st1", "Preference", "User prefers dark theme in all apps", "3m ago", 0.95f),
                    MemoryEntry("st2", "Task", "Current task: researching LLM benchmarks", "8m ago", 0.88f),
                    MemoryEntry("st3", "Activity", "Opened browser to github.com/hermes", "12m ago", 0.72f),
                    MemoryEntry("lt1", "Profile", "User is a software developer working on AI systems", "2d ago", 0.92f),
                    MemoryEntry("lt2", "Preference", "Preferred programming languages: Kotlin, Python, TypeScript", "3d ago", 0.85f),
                    MemoryEntry("sem1", "Knowledge", "Hermes is an AI agent framework with multiple agent types", "1d ago", 0.90f),
                    MemoryEntry("sem2", "Architecture", "The companion app connects to a Hermes brain via WebSocket", "1d ago", 0.85f)
                )
            )
        }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}