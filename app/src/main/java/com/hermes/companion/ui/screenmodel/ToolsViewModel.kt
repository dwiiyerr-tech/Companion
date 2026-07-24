1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.lifecycle.ViewModel
4|import kotlinx.coroutines.flow.MutableStateFlow
5|import kotlinx.coroutines.flow.StateFlow
6|import kotlinx.coroutines.flow.asStateFlow
7|import kotlinx.coroutines.flow.update
8|import com.hermes.companion.ui.theme.*
9|
10|class ToolsViewModel : ViewModel() {
11|
12|    private val _uiState = MutableStateFlow(ToolsUiState())
13|    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()
14|
15|    init {
16|        loadTools()
17|    }
18|
19|    private fun loadTools() {
20|        _uiState.update {
21|            it.copy(
22|                tools = listOf(
23|                    ToolInfo("web_search", "Web Search", ToolCategory.WEB, "Search the web for information", true, "active"),
24|                    ToolInfo("web_scrape", "Web Scraper", ToolCategory.WEB, "Extract content from web pages", true, "active"),
25|                    ToolInfo("http_request", "HTTP Client", ToolCategory.WEB, "Make HTTP requests to APIs", true, "active"),
26|                    ToolInfo("terminal", "Terminal", ToolCategory.TERMINAL, "Execute shell commands", true, "active"),
27|                    ToolInfo("python_repl", "Python REPL", ToolCategory.TERMINAL, "Run Python code", true, "active"),
28|                    ToolInfo("browser_navigate", "Browser Navigate", ToolCategory.BROWSER, "Navigate to URLs", true, "active"),
29|                    ToolInfo("browser_click", "Browser Click", ToolCategory.BROWSER, "Click elements on page", true, "active"),
30|                    ToolInfo("browser_screenshot", "Browser Screenshot", ToolCategory.BROWSER, "Capture page screenshot", true, "active"),
31|                    ToolInfo("vision_analyze", "Vision Analyze", ToolCategory.VISION, "Analyze images with AI", false, "idle"),
32|                    ToolInfo("vision_ocr", "Vision OCR", ToolCategory.VISION, "Extract text from images", false, "idle"),
33|                    ToolInfo("tts", "Text to Speech", ToolCategory.VOICE, "Convert text to audio", false, "idle"),
34|                    ToolInfo("stt", "Speech to Text", ToolCategory.VOICE, "Convert audio to text", false, "idle"),
35|                    ToolInfo("memory_store", "Memory Store", ToolCategory.MEMORY, "Store memory entries", true, "active"),
36|                    ToolInfo("memory_retrieve", "Memory Retrieve", ToolCategory.MEMORY, "Retrieve memory entries", true, "active"),
37|                    ToolInfo("file_read", "File Read", ToolCategory.FILE, "Read file contents", true, "active"),
38|                    ToolInfo("file_write", "File Write", ToolCategory.FILE, "Write file contents", true, "active"),
39|                    ToolInfo("sql_query", "SQL Query", ToolCategory.DATABASE, "Execute SQL queries", false, "idle"),
40|                    ToolInfo("automation_schedule", "Task Scheduler", ToolCategory.AUTOMATION, "Schedule recurring tasks", false, "idle"),
41|                    ToolInfo("automation_webhook", "Webhook Handler", ToolCategory.AUTOMATION, "Handle incoming webhooks", true, "active")
42|                )
43|            )
44|        }
45|    }
46|
47|    fun filterCategory(category: ToolCategory?) {
48|        _uiState.update { it.copy(selectedCategory = category) }
49|    }
50|
51|    fun toggleTool(toolId: String) {
52|        _uiState.update {
53|            it.copy(
54|                tools = it.tools.map { tool ->
55|                    if (tool.id == toolId) tool.copy(isEnabled = !tool.isEnabled) else tool
56|                }
57|            )
58|        }
59|    }
60|
61|    fun refresh() {
62|        loadTools()
63|    }
64|}