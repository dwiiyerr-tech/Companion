package com.hermes.companion.ui.screenmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.hermes.companion.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.ViewModel

data class ActivityEntry(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val type: String,
    val color: Color
)

data class HomeUiState(
    val brainConnected: Boolean = false,
    val brainHost: String = "ws://192.168.1.100:8765",
    val brainLatencyMs: Long = 0,
    val missionsRunning: Int = 0,
    val agentsActive: Int = 0,
    val uptime: String = "0m",
    val cpuUsage: Float = 0f,
    val ramUsage: Float = 0f,
    val batteryLevel: Int = 100,
    val recentActivity: List<ActivityEntry> = emptyList()
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        _uiState.update {
            HomeUiState(
                brainConnected = true,
                brainHost = "ws://192.168.1.100:8765",
                brainLatencyMs = 23,
                missionsRunning = 3,
                agentsActive = 7,
                uptime = "2h 34m",
                cpuUsage = 34f,
                ramUsage = 62f,
                batteryLevel = 78,
                recentActivity = listOf(
                    ActivityEntry(
                        icon = Icons.Filled.PlayArrow,
                        title = "Mission completed",
                        subtitle = "Web research: AI news roundup",
                        timeAgo = "2m ago",
                        type = "mission",
                        color = StatusGreen
                    ),
                    ActivityEntry(
                        icon = Icons.Filled.Agent,
                        title = "Agent task assigned",
                        subtitle = "Browser agent → scrape page",
                        timeAgo = "5m ago",
                        type = "agent",
                        color = HermesPurpleLight
                    ),
                    ActivityEntry(
                        icon = Icons.Filled.Memory,
                        title = "Memory consolidated",
                        subtitle = "Short-term → Long-term (12 items)",
                        timeAgo = "12m ago",
                        type = "memory",
                        color = StatusCyan
                    ),
                    ActivityEntry(
                        icon = Icons.Filled.PhoneAndroid,
                        title = "Screenshot captured",
                        subtitle = "Device home screen",
                        timeAgo = "18m ago",
                        type = "android",
                        color = StatusYellow
                    ),
                    ActivityEntry(
                        icon = Icons.Filled.Error,
                        title = "Agent error resolved",
                        subtitle = "Vision agent timeout → retry succeeded",
                        timeAgo = "25m ago",
                        type = "error",
                        color = StatusOrange
                    )
                )
            )
        }
    }

    fun refresh() {
        loadState()
    }
}
