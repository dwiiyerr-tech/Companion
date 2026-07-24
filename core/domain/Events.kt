package com.hermes.companion.core.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class AppEvent {
    data class BrainConnected(val url: String, val timestamp: Long = System.currentTimeMillis()) : AppEvent()
    data class BrainDisconnected(val reason: String? = null, val timestamp: Long = System.currentTimeMillis()) : AppEvent()
    data class BrainReconnecting(val attempt: Int, val nextRetryMs: Long) : AppEvent()

    data class MissionUpdate(val mission: Mission) : AppEvent()
    data class MissionCreated(val mission: Mission) : AppEvent()
    data class MissionCompleted(val missionId: String) : AppEvent()
    data class MissionFailed(val missionId: String, val error: String) : AppEvent()

    data class AgentHealth(val agent: Agent) : AppEvent()
    data class AgentStarted(val agentId: String) : AppEvent()
    data class AgentStopped(val agentId: String) : AppEvent()
    data class AgentError(val agentId: String, val error: String) : AppEvent()

    data class LogReceived(val log: com.hermes.companion.core.domain.Log) : AppEvent()

    data class NotificationReceived(
        val packageName: String,
        val title: String,
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent()

    data class ScreenCaptured(val timestamp: Long = System.currentTimeMillis()) : AppEvent()
    data class VoiceTranscript(val text: String, val isFinal: Boolean) : AppEvent()

    data class OverlayToggle(val visible: Boolean) : AppEvent()

    data class Error(
        val code: String,
        val message: String,
        val details: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent()

    data class StateUpdate(val state: AppState) : AppEvent()

    object ConnectionHealthy : AppEvent()
    object SyncComplete : AppEvent()
}
