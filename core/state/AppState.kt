package com.hermes.companion.core.state

import com.hermes.companion.core.domain.Agent
import com.hermes.companion.core.domain.Mission
import com.hermes.companion.core.network.HermesWebSocketClient

/**
 * Global application state data classes, used by ViewModels
 * and the EventBus to drive UI updates.
 */
data class AppState(
    val connection: ConnectionState = ConnectionState(),
    val missions: List<Mission> = emptyList(),
    val activeMissionId: String? = null,
    val agents: List<Agent> = emptyList(),
    val overlayVisible: Boolean = false,
    val voiceListening: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val settings: AppSettings = AppSettings()
)

data class ConnectionState(
    val status: Status = Status.DISCONNECTED,
    val serverUrl: String = "",
    val lastHeartbeat: Long = 0L,
    val reconnectAttempt: Int = 0,
    val errorMessage: String? = null
) {
    enum class Status {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        RECONNECTING
    }

    companion object {
        fun fromWebSocketState(ws: HermesWebSocketClient.ConnectionState): Status = when (ws) {
            HermesWebSocketClient.ConnectionState.DISCONNECTED -> Status.DISCONNECTED
            HermesWebSocketClient.ConnectionState.CONNECTING -> Status.CONNECTING
            HermesWebSocketClient.ConnectionState.CONNECTED -> Status.CONNECTED
            HermesWebSocketClient.ConnectionState.RECONNECTING -> Status.RECONNECTING
            HermesWebSocketClient.ConnectionState.CLOSING -> Status.DISCONNECTED
        }
    }
}

data class AppSettings(
    val brainUrl: String = "wss://brain.hermes.ai/ws",
    val authToken: String = "",
    val autoConnect: Boolean = true,
    val overlayOpacity: Float = 0.85f,
    val heartbeatIntervalMs: Long = 30_000L,
    val logRetentionDays: Int = 7,
    val theme: ThemeMode = ThemeMode.DARK
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

data class MissionDetailState(
    val mission: Mission? = null,
    val loading: Boolean = true,
    val error: String? = null
)

data class DashboardState(
    val connected: Boolean = false,
    val activeMissions: Int = 0,
    val activeAgents: Int = 0,
    val recentLogs: Int = 0,
    val systemHealth: Float = 1f
)