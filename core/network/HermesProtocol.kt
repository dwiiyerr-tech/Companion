package com.hermes.companion.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Wire Protocol ──────────────────────────────────────────────

@Serializable
sealed class HermesMessage {
    abstract val type: String
    abstract val id: String
    abstract val timestamp: Long
}

// ── Outbound (companion → brain) ──────────────────────────────

@Serializable
@SerialName("command")
data class HermesCommand(
    override val id: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val command: CommandType,
    val payload: Map<String, String>? = null,
    val authToken: String? = null
) : HermesMessage() {
    override val type: String = "command"
}

@Serializable
enum class CommandType {
    AUTH,
    HEARTBEAT,
    SCREEN_CAPTURE,
    START_MISSION,
    STOP_MISSION,
    PAUSE_MISSION,
    RESUME_MISSION,
    GET_STATUS,
    SYNC_DATA,
    EXECUTE_ACTION,
    VOICE_INPUT,
    NOTIFICATION_REPLY,
    SYSTEM_COMMAND
}

// ── Inbound (brain → companion) ───────────────────────────────

@Serializable
@SerialName("response")
data class HermesResponse(
    override val id: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val commandId: String,
    val success: Boolean,
    val data: Map<String, String>? = null,
    val error: String? = null
) : HermesMessage() {
    override val type: String = "response"
}

@Serializable
@SerialName("event")
data class HermesEvent(
    override val id: String,
    override val timestamp: Long = System.currentTimeMillis(),
    val event: EventType,
    val payload: Map<String, String>? = null
) : HermesMessage() {
    override val type: String = "event"
}

@Serializable
enum class EventType {
    MISSION_UPDATE,
    AGENT_HEALTH,
    AGENT_STATUS,
    LOG_ENTRY,
    NOTIFICATION,
    SCREEN_ANALYSIS,
    VOICE_COMMAND,
    HEARTBEAT_ACK,
    SYNC_REQUIRED,
    ERROR,
    STATE_UPDATE
}

// ── Auth ──────────────────────────────────────────────────────

@Serializable
data class AuthPayload(
    val token: String,
    val deviceId: String,
    val deviceName: String,
    val appVersion: String
)

@Serializable
data class HeartbeatPayload(
    val connectedSince: Long,
    val pendingMessages: Int
)
