package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ToolCategory { WEB, BROWSER, TERMINAL, VISION, VOICE, MEMORY, FILE, DATABASE, AUTOMATION }

data class ToolInfo(
    val id: String,
    val name: String,
    val category: ToolCategory,
    val description: String,
    val isEnabled: Boolean = true,
    val status: String = "active"
)

data class ToolsUiState(
    val tools: List<ToolInfo> = emptyList(),
    val selectedCategory: ToolCategory? = null
)

class ToolsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init { loadTools() }

    private fun loadTools() {
        _uiState.update {
            it.copy(
                tools = listOf(
                    ToolInfo("web_search", "Web Search", ToolCategory.WEB, "Search the web for information"),
                    ToolInfo("web_scrape", "Web Scraper", ToolCategory.WEB, "Extract content from web pages"),
                    ToolInfo("terminal", "Terminal", ToolCategory.TERMINAL, "Execute shell commands"),
                    ToolInfo("python_repl", "Python REPL", ToolCategory.TERMINAL, "Run Python code"),
                    ToolInfo("browser_navigate", "Browser Navigate", ToolCategory.BROWSER, "Navigate to URLs"),
                    ToolInfo("browser_click", "Browser Click", ToolCategory.BROWSER, "Click elements on page"),
                    ToolInfo("browser_screenshot", "Browser Screenshot", ToolCategory.BROWSER, "Capture page screenshot"),
                    ToolInfo("vision_analyze", "Vision Analyze", ToolCategory.VISION, "Analyze images with AI", isEnabled = false, status = "idle"),
                    ToolInfo("tts", "Text to Speech", ToolCategory.VOICE, "Convert text to audio", isEnabled = false, status = "idle"),
                    ToolInfo("memory_store", "Memory Store", ToolCategory.MEMORY, "Store memory entries"),
                    ToolInfo("file_read", "File Read", ToolCategory.FILE, "Read file contents"),
                    ToolInfo("sql_query", "SQL Query", ToolCategory.DATABASE, "Execute SQL queries", isEnabled = false, status = "idle"),
                    ToolInfo("automation_schedule", "Task Scheduler", ToolCategory.AUTOMATION, "Schedule recurring tasks", isEnabled = false, status = "idle")
                )
            )
        }
    }

    fun filterCategory(category: ToolCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleTool(toolId: String) {
        _uiState.update {
            it.copy(tools = it.tools.map { tool ->
                if (tool.id == toolId) tool.copy(isEnabled = !tool.isEnabled) else tool
            })
        }
    }
}
