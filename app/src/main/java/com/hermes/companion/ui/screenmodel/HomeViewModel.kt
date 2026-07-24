1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.compose.material.icons.Icons
4|import androidx.compose.material.icons.filled.*
5|import androidx.compose.ui.graphics.Color
6|import com.hermes.companion.ui.theme.*
7|import kotlinx.coroutines.flow.MutableStateFlow
8|import kotlinx.coroutines.flow.StateFlow
9|import kotlinx.coroutines.flow.asStateFlow
10|import kotlinx.coroutines.flow.update
11|import androidx.lifecycle.ViewModel
12|
13|data class ActivityEntry(
14|    val icon: androidx.compose.ui.graphics.vector.ImageVector,
15|    val title: String,
16|    val subtitle: String,
17|    val timeAgo: String,
18|    val type: String,
19|    val color: Color
20|)
21|
22|data class HomeUiState(
23|    val brainConnected: Boolean = false,
24|    val brainHost: String = "ws://192.168.1.100:8765",
25|    val brainLatencyMs: Long = 0,
26|    val missionsRunning: Int = 0,
27|    val agentsActive: Int = 0,
28|    val uptime: String = "0m",
29|    val cpuUsage: Float = 0f,
30|    val ramUsage: Float = 0f,
31|    val batteryLevel: Int = 100,
32|    val recentActivity: List<ActivityEntry> = emptyList()
33|)
34|
35|class HomeViewModel : ViewModel() {
36|
37|    private val _uiState = MutableStateFlow(HomeUiState())
38|    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
39|
40|    init {
41|        loadState()
42|    }
43|
44|    private fun loadState() {
45|        _uiState.update {
46|            HomeUiState(
47|                brainConnected = true,
48|                brainHost = "ws://192.168.1.100:8765",
49|                brainLatencyMs = 23,
50|                missionsRunning = 3,
51|                agentsActive = 7,
52|                uptime = "2h 34m",
53|                cpuUsage = 34f,
54|                ramUsage = 62f,
55|                batteryLevel = 78,
56|                recentActivity = listOf(
57|                    ActivityEntry(
58|                        icon = Icons.Filled.PlayArrow,
59|                        title = "Mission completed",
60|                        subtitle = "Web research: AI news roundup",
61|                        timeAgo = "2m ago",
62|                        type = "mission",
63|                        color = StatusGreen
64|                    ),
65|                    ActivityEntry(
66|                        icon = Icons.Filled.Agent,
67|                        title = "Agent task assigned",
68|                        subtitle = "Browser agent → scrape page",
69|                        timeAgo = "5m ago",
70|                        type = "agent",
71|                        color = HermesPurpleLight
72|                    ),
73|                    ActivityEntry(
74|                        icon = Icons.Filled.Memory,
75|                        title = "Memory consolidated",
76|                        subtitle = "Short-term → Long-term (12 items)",
77|                        timeAgo = "12m ago",
78|                        type = "memory",
79|                        color = StatusCyan
80|                    ),
81|                    ActivityEntry(
82|                        icon = Icons.Filled.PhoneAndroid,
83|                        title = "Screenshot captured",
84|                        subtitle = "Device home screen",
85|                        timeAgo = "18m ago",
86|                        type = "android",
87|                        color = StatusYellow
88|                    ),
89|                    ActivityEntry(
90|                        icon = Icons.Filled.Error,
91|                        title = "Agent error resolved",
92|                        subtitle = "Vision agent timeout → retry succeeded",
93|                        timeAgo = "25m ago",
94|                        type = "error",
95|                        color = StatusOrange
96|                    )
97|                )
98|            )
99|        }
100|    }
101|
102|    fun refresh() {
103|        loadState()
104|    }
105|}
106|