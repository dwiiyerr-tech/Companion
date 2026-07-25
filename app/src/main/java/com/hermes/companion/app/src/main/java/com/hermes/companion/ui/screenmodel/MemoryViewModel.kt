package com.hermes.companion.ui.screenmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MemoryTab { SHORT_TERM, LONG_TERM, SEMANTIC, KNOWLEDGE_GRAPH }

data class MemoryItem(
    val id: String,
    val content: String,
    val description: String,
    val timeAgo: String,
    val relevance: Float
)

data class MemoryUiState(
    val memories: List<MemoryItem> = emptyList(),
    val selectedTab: MemoryTab = MemoryTab.SHORT_TERM,
    val searchQuery: String = ""
)

class MemoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    var showAddDialog by mutableStateOf(false)

    init {
        loadMemories()
    }

    private fun loadMemories() {
        _uiState.update {
            it.copy(
                memories = listOf(
                    MemoryItem("st1", "User prefers dark theme in all apps", "Preference", "3m ago", 0.95f),
                    MemoryItem("st2", "Current task: researching LLM benchmarks", "Task", "8m ago", 0.88f),
                    MemoryItem("st3", "Opened browser to github.com/hermes", "Activity", "12m ago", 0.72f),
                    MemoryItem("lt1", "User is a software developer working on AI systems", "Profile", "2d ago", 0.92f),
                    MemoryItem("lt2", "Preferred programming languages: Kotlin, Python, TypeScript", "Preference", "3d ago", 0.85f),
                    MemoryItem("sem1", "Hermes is an AI agent framework with multiple agent types", "Knowledge", "1d ago", 0.90f),
                    MemoryItem("sem2", "The companion app connects to a Hermes brain via WebSocket", "Architecture", "1d ago", 0.85f)
                )
            )
        }
    }

    fun selectTab(tab: MemoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
