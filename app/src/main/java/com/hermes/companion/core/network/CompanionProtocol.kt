package com.hermes.companion.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.hermes.companion.core.domain.LogLevel

// ──────────────────────────────────────────────
// Top-Level Envelope
// ──────────────────────────────────────────────

@Serializable
data class BrainMessage(
    val type: String,
    val id: String? = null,
    val payload: String? = null,                       // JSON-encoded inner payload
    val timestamp: Long = 0L,
    val source: String? = null,
    val target: String? = null
)

@Serializable
data class CompanionMessage(
    val type: String,
    val id: String? = null,
    val payload: String? = null,
    val timestamp: Long = 0L
)

// ──────────────────────────────────────────────
// Mission Types
// ──────────────────────────────────────────────

@Serializable
data class MissionRequest(
    val name: String,
    val goals: List<String>,
    @SerialName("agent_types") val agentTypes: List<String> = emptyList(),
    val priority: MissionPriority = MissionPriority.NORMAL,
    val context: Map<String, String> = emptyMap(),
    val timeout: Long = 300_000L,
    val plugins: List<String> = emptyList()
)

@Serializable
enum class MissionPriority {
    LOW, NORMAL, HIGH, CRITICAL
}

@Serializable
data class MissionResponse(
    @SerialName("mission_id") val missionId: String,
    val status: MissionStatus,
    @SerialName("created_at") val createdAt: Long,
    val error: String? = null
)

@Serializable
data class MissionStatus(
    @SerialName("mission_id") val missionId: String,
    val state: MissionState,
    val progress: Float = 0f,
    val step: String? = null,
    val message: String? = null,
    @SerialName("started_at") val startedAt: Long? = null,
    @SerialName("completed_at") val completedAt: Long? = null,
    val error: String? = null
)

@Serializable
enum class MissionState {
    QUEUED, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

// ──────────────────────────────────────────────
// Agent Types
// ──────────────────────────────────────────────

@Serializable
data class AgentStatus(
    @SerialName("agent_id") val agentId: String,
    val name: String,
    val type: String,
    val status: AgentHealthState,
    @SerialName("current_task") val currentTask: String? = null,
    @SerialName("uptime_ms") val uptimeMs: Long = 0L,
    @SerialName("cpu_percent") val cpuPercent: Float = 0f,
    @SerialName("ram_mb") val ramMb: Float = 0f,
    @SerialName("task_count") val taskCount: Int = 0,
    @SerialName("last_seen") val lastSeen: Long = 0L,
    val version: String? = null,
    val capabilities: List<String> = emptyList()
)

@Serializable
enum class AgentHealthState {
    HEALTHY, DEGRADED, UNRESPONSIVE, ERROR, OFFLINE
}

@Serializable
data class AgentTask(
    @SerialName("task_id") val taskId: String,
    @SerialName("agent_id") val agentId: String,
    val type: String,
    val status: String,
    val description: String,
    @SerialName("started_at") val startedAt: Long,
    @SerialName("completed_at") val completedAt: Long? = null,
    val result: String? = null,
    val error: String? = null
)

@Serializable
data class AgentAction(
    val action: String,
    val params: Map<String, String> = emptyMap()
)

// ──────────────────────────────────────────────
// Memory Types
// ──────────────────────────────────────────────

@Serializable
data class MemoryQuery(
    val text: String? = null,
    val type: MemoryType? = null,
    val tags: List<String> = emptyList(),
    val limit: Int = 20,
    val offset: Int = 0,
    val threshold: Float = 0.7f
)

@Serializable
enum class MemoryType {
    SHORT_TERM, LONG_TERM, SEMANTIC, EPISODIC, PROCEDURAL
}

@Serializable
data class MemoryEntry(
    val id: String,
    val content: String,
    val type: MemoryType,
    val tags: List<String> = emptyList(),
    val source: String? = null,
    val importance: Float = 0.5f,
    val embedding: List<Float>? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("accessed_at") val accessedAt: Long? = null,
    val ttl: Long? = null
)

@Serializable
data class MemoryResult(
    val entries: List<MemoryEntry>,
    val total: Int = 0,
    val query: String? = null
)

// ──────────────────────────────────────────────
// Log & Event Types
// ──────────────────────────────────────────────

@Serializable
data class LogEvent(
    val id: String? = null,
    val level: LogLevel,
    val source: String,
    val message: String,
    val data: Map<String, String> = emptyMap(),
    val timestamp: Long = 0L,
    val thread: String? = null,
    val stacktrace: String? = null
)

@Serializable
data class LogFilter(
    val levels: List<LogLevel> = emptyList(),
    val sources: List<String> = emptyList(),
    val query: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val limit: Int = 100,
    val offset: Int = 0
)

@Serializable
data class LogExport(
    val format: ExportFormat = ExportFormat.JSON,
    val filter: LogFilter? = null,
    val path: String? = null
)

@Serializable
enum class ExportFormat {
    JSON, CSV, PLAINTEXT
}

// ──────────────────────────────────────────────
// Performance & System Types
// ──────────────────────────────────────────────

@Serializable
data class PerformanceMetrics(
    @SerialName("cpu_percent") val cpuPercent: Float = 0f,
    @SerialName("cpu_temp") val cpuTemp: Float? = null,
    @SerialName("ram_total_mb") val ramTotalMb: Long = 0L,
    @SerialName("ram_used_mb") val ramUsedMb: Long = 0L,
    @SerialName("ram_percent") val ramPercent: Float = 0f,
    @SerialName("battery_percent") val batteryPercent: Int = 0,
    @SerialName("battery_charging") val batteryCharging: Boolean = false,
    @SerialName("disk_total_mb") val diskTotalMb: Long = 0L,
    @SerialName("disk_used_mb") val diskUsedMb: Long = 0L,
    @SerialName("network_rx_kbps") val networkRxKbps: Long = 0L,
    @SerialName("network_tx_kbps") val networkTxKbps: Long = 0L,
    @SerialName("process_count") val processCount: Int = 0,
    @SerialName("uptime_ms") val uptimeMs: Long = 0L,
    @SerialName("measured_at") val measuredAt: Long = 0L
)

@Serializable
data class SystemStatus(
    val brain: BrainStatusInfo? = null,
    val companion: CompanionStatusInfo? = null,
    val agents: Int = 0,
    val missions: Int = 0,
    val memory: MemoryStats? = null,
    val plugins: Int = 0,
    val uptime: Long = 0L
)

@Serializable
data class BrainStatusInfo(
    val connected: Boolean = false,
    val version: String? = null,
    val uptime: Long = 0L,
    @SerialName("active_missions") val activeMissions: Int = 0,
    @SerialName("active_agents") val activeAgents: Int = 0,
    val load: Float = 0f
)

@Serializable
data class CompanionStatusInfo(
    val connected: Boolean = false,
    val version: String? = null,
    val uptime: Long = 0L,
    @SerialName("services_running") val servicesRunning: List<String> = emptyList(),
    val permissions: Map<String, Boolean> = emptyMap()
)

@Serializable
data class MemoryStats(
    val total: Int = 0,
    @SerialName("short_term") val shortTerm: Int = 0,
    @SerialName("long_term") val longTerm: Int = 0,
    val semantic: Int = 0,
    val episodic: Int = 0,
    val procedural: Int = 0
)

// ──────────────────────────────────────────────
// Workflow Types
// ──────────────────────────────────────────────

@Serializable
data class WorkflowDefinition(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val nodes: List<WorkflowNode> = emptyList(),
    val edges: List<WorkflowEdge> = emptyList(),
    @SerialName("created_at") val createdAt: Long = 0L,
    val version: Int = 1
)

@Serializable
data class WorkflowNode(
    val id: String,
    val type: WorkflowNodeType,
    val label: String,
    val description: String? = null,
    val config: Map<String, String> = emptyList().toMap(),
    val position: NodePosition? = null,
    val status: String? = null
)

@Serializable
enum class WorkflowNodeType {
    START, END, GOAL, PLANNER, REASONING, AGENT, TOOL, EXECUTION, VERIFICATION, RECOVERY, DECISION, ACTION, CONDITION
}

@Serializable
data class WorkflowEdge(
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    val label: String? = null,
    val condition: String? = null
)

@Serializable
data class NodePosition(
    val x: Float = 0f,
    val y: Float = 0f
)

@Serializable
data class WorkflowExecution(
    @SerialName("execution_id") val executionId: String,
    @SerialName("workflow_id") val workflowId: String,
    @SerialName("current_node") val currentNode: String? = null,
    val status: WorkflowExecutionStatus,
    val progress: Float = 0f,
    @SerialName("node_statuses") val nodeStatuses: Map<String, String> = emptyMap(),
    @SerialName("started_at") val startedAt: Long,
    @SerialName("completed_at") val completedAt: Long? = null
)

@Serializable
enum class WorkflowExecutionStatus {
    RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

// ──────────────────────────────────────────────
// Plugin Types
// ──────────────────────────────────────────────

@Serializable
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val author: String? = null,
    val permissions: List<String> = emptyList(),
    val capabilities: List<String> = emptyList(),
    val hooks: List<String> = emptyList(),
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class PluginStatus(
    @SerialName("plugin_id") val pluginId: String,
    val enabled: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null,
    @SerialName("memory_kb") val memoryKb: Long = 0L,
    @SerialName("started_at") val startedAt: Long? = null
)

// ──────────────────────────────────────────────
// UI Tree (Accessibility)
// ──────────────────────────────────────────────

@Serializable
data class UITreeNode(
    @SerialName("node_id") val nodeId: String,
    @SerialName("class_name") val className: String? = null,
    val text: String? = null,
    @SerialName("content_description") val contentDescription: String? = null,
    val packageName: String? = null,
    val bounds: Bounds? = null,
    @SerialName("is_clickable") val isClickable: Boolean = false,
    @SerialName("is_focusable") val isFocusable: Boolean = false,
    @SerialName("is_scrollable") val isScrollable: Boolean = false,
    @SerialName("is_checked") val isChecked: Boolean? = null,
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("is_password") val isPassword: Boolean = false,
    @SerialName("is_selected") val isSelected: Boolean = false,
    val children: List<UITreeNode> = emptyList(),
    @SerialName("child_count") val childCount: Int = 0,
    val actions: List<String> = emptyList(),
    @SerialName("window_id") val windowId: Int = -1,
    val depth: Int = 0
)

@Serializable
data class Bounds(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0
) {
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}

@Serializable
data class GestureRequest(
    val type: GestureType,
    @SerialName("start_x") val startX: Float = 0f,
    @SerialName("start_y") val startY: Float = 0f,
    @SerialName("end_x") val endX: Float? = null,
    @SerialName("end_y") val endY: Float? = null,
    val duration: Long = 100L,
    val steps: Int = 10,
    val text: String? = null
)

@Serializable
enum class GestureType {
    CLICK, LONG_CLICK, DOUBLE_CLICK, SCROLL_UP, SCROLL_DOWN, SCROLL_LEFT, SCROLL_RIGHT,
    SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT, TYPE, BACK, HOME, RECENT_APPS
}

// ──────────────────────────────────────────────
// Notification Types
// ──────────────────────────────────────────────

@Serializable
data class HermesNotification(
    val id: String,
    @SerialName("package_name") val packageName: String,
    val title: String? = null,
    val body: String? = null,
    val category: String? = null,
    val priority: Int = 0,
    @SerialName("posted_at") val postedAt: Long,
    @SerialName("is_ongoing") val isOngoing: Boolean = false,
    @SerialName("can_reply") val canReply: Boolean = false,
    @SerialName("has_actions") val hasActions: Boolean = false,
    val actions: List<NotificationAction> = emptyList(),
    val source: String? = null,
    val key: String? = null
)

@Serializable
data class NotificationAction(
    val id: String,
    val label: String,
    val type: NotificationActionType = NotificationActionType.DEFAULT
)

@Serializable
enum class NotificationActionType {
    DEFAULT, REPLY, OPEN, DISMISS, SNOOZE
}

// ──────────────────────────────────────────────
// Connection & Auth Types
// ──────────────────────────────────────────────

@Serializable
data class ConnectionConfig(
    val host: String = "127.0.0.1",
    val port: Int = 9876,
    @SerialName("auth_token") val authToken: String? = null,
    @SerialName("use_ssl") val useSsl: Boolean = false,
    @SerialName("reconnect_enabled") val reconnectEnabled: Boolean = true,
    @SerialName("reconnect_interval_ms") val reconnectIntervalMs: Long = 5000L,
    @SerialName("heartbeat_interval_ms") val heartbeatIntervalMs: Long = 30_000L,
    @SerialName("max_reconnect_attempts") val maxReconnectAttempts: Int = 10
)

@Serializable
data class HandshakePayload(
    val type: String = "handshake",
    val client: String = "android_companion",
    val version: String? = null,
    val capabilities: List<String> = emptyList(),
    val device: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    @SerialName("session_id") val sessionId: String? = null,
    val error: String? = null,
    val permissions: List<String> = emptyList()
)

// ──────────────────────────────────────────────
// Screen Capture Types
// ──────────────────────────────────────────────

@Serializable
data class ScreenshotResult(
    @SerialName("image_base64") val imageBase64: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val format: String = "png",
    val error: String? = null,
    @SerialName("captured_at") val capturedAt: Long = 0L
)

@Serializable
data class ScreenCaptureConfig(
    val continuous: Boolean = false,
    val intervalMs: Long = 1000L,
    val maxFrames: Int = 10,
    val quality: Int = 80,
    val format: String = "jpeg",
    @SerialName("max_dimension") val maxDimension: Int = 1024
)

// ──────────────────────────────────────────────
// Voice Types
// ──────────────────────────────────────────────

@Serializable
data class VoiceCommand(
    val text: String,
    val confidence: Float = 0f,
    val language: String = "en-US",
    val wakeWordDetected: Boolean = false,
    val timestamp: Long = 0L
)

@Serializable
data class VoiceResponse(
    val text: String,
    val action: String? = null,
    val params: Map<String, String> = emptyMap(),
    val error: String? = null
)

// ──────────────────────────────────────────────
// Generic Command / Response Types
// ──────────────────────────────────────────────

@Serializable
data class CommandRequest(
    val command: String,
    val params: Map<String, String> = emptyMap(),
    val id: String? = null
)

@Serializable
data class CommandResponse(
    val success: Boolean,
    val data: String? = null,
    val error: String? = null,
    @SerialName("command_id") val commandId: String? = null
)

@Serializable
data class ErrorInfo(
    val code: String,
    val message: String,
    val details: String? = null,
    val recoverable: Boolean = false
)
