1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.lifecycle.ViewModel
4|import kotlinx.coroutines.flow.MutableStateFlow
5|import kotlinx.coroutines.flow.StateFlow
6|import kotlinx.coroutines.flow.asStateFlow
7|import kotlinx.coroutines.flow.update
8|
9|class MemoryViewModel : ViewModel() {
10|
11|    private val _uiState = MutableStateFlow(MemoryUiState())
12|    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()
13|
14|    init {
15|        loadMemories()
16|    }
17|
18|    private fun loadMemories() {
19|        _uiState.update {
20|            it.copy(
21|                memories = mapOf(
22|                    MemoryTab.SHORT_TERM to listOf(
23|                        MemoryItem("st1", "User prefers dark theme in all apps", "Dark theme preference", "3m ago", 0.95f),
24|                        MemoryItem("st2", "Current task: researching LLM benchmarks", "LLM benchmark research task", "8m ago", 0.88f),
25|                        MemoryItem("st3", "Opened browser to github.com/hermes", "GitHub browsing session", "12m ago", 0.72f),
26|                        MemoryItem("st4", "Scheduled mission for tomorrow at 9am", "Scheduled mission", "15m ago", 0.65f),
27|                        MemoryItem("st5", "Last used voice command 5 minutes ago", "Voice command activity", "18m ago", 0.55f)
28|                    ),
29|                    MemoryTab.LONG_TERM to listOf(
30|                        MemoryItem("lt1", "User is a software developer working on AI systems", "Developer profile", "2d ago", 0.92f),
31|                        MemoryItem("lt2", "Preferred programming languages: Kotlin, Python, TypeScript", "Language preferences", "3d ago", 0.85f),
32|                        MemoryItem("lt3", "Work schedule: typically active 9am-6pm EST", "Work pattern", "5d ago", 0.78f),
33|                        MemoryItem("lt4", "Often searches for machine learning papers on arxiv", "Research patterns", "7d ago", 0.70f),
34|                        MemoryItem("lt5", "Uses GitHub Copilot and VS Code as primary IDE", "Development tools", "10d ago", 0.65f)
35|                    ),
36|                    MemoryTab.SEMANTIC to listOf(
37|                        MemoryItem("sem1", "Hermes is an AI agent framework with multiple agent types", "Hermes framework knowledge", "1d ago", 0.90f),
38|                        MemoryItem("sem2", "The companion app connects to a Hermes brain via WebSocket", "Companion app architecture", "1d ago", 0.85f),
39|                        MemoryItem("sem3", "Missions can be running, queued, completed, or failed states", "Mission state machine", "3d ago", 0.75f),
40|                        MemoryItem("sem4", "Android control requires accessibility permissions", "Android permissions knowledge", "5d ago", 0.70f)
41|                    ),
42|                    MemoryTab.KNOWLEDGE_GRAPH to listOf(
43|                        MemoryItem("kg1", "Hermes → has_agent → Planner, Reasoner, Browser, Vision", "Agent relationships", "1d ago", 0.88f),
44|                        MemoryItem("kg2", "Mission → runs_on → Agent, Mission → has_status → State", "Mission structure", "2d ago", 0.82f),
45|                        MemoryItem("kg3", "Memory → has_type → ShortTerm, LongTerm, Semantic, KG", "Memory taxonomy", "3d ago", 0.78f),
46|                        MemoryItem("kg4", "Plugin → extends → ToolRegistry, Plugin → has_version → SemVer", "Plugin system", "5d ago", 0.72f)
47|                    )
48|                )
49|            )
50|        }
51|    }
52|
53|    fun selectTab(tab: MemoryTab) {
54|        _uiState.update { it.copy(selectedTab = tab) }
55|    }
56|
57|    fun search(query: String) {
58|        _uiState.update { it.copy(searchQuery = query) }
59|    }
60|}