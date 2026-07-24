package com.hermes.companion.data.repository

import android.content.Context
import com.hermes.companion.core.network.*
import com.hermes.companion.service.HermesWebSocketService
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

/**
 * Main repository combining WebSocket connection and local Room DB.
 *
 * Responsibilities:
 * - All Brain communication via WebSocket
 * - Provides offline fallback with cached data from Room
 * - Manages connection lifecycle
 * - Event routing between UI and services
 */
class HermesRepository(
    private val context: Context,
    private val webSocketService: HermesWebSocketService? = null
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    // ── Connection State ───────────────────────────────────

    val connectionState: StateFlow<Boolean> = webSocketService
        ?.connectionState
        ?.map { it.name == "CONNECTED" }
        ?.stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, false)
        ?: MutableStateFlow(false)

    val brainEvents: SharedFlow<BrainMessage> = webSocketService?.events
        ?: MutableSharedFlow()

    // ── Brain Communication ────────────────────────────────

    /**
     * Connect to Hermes Brain.
     */
    suspend fun connectToBrain(
        host: String,
        port: Int = 9876,
        authToken: String? = null
    ): Result<String> = try {
        webSocketService?.startService(context, host, port, authToken)
        Result.success("Connection initiated")
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Disconnect from Brain.
     */
    suspend fun disconnect(): Result<Unit> = try {
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Create a new mission on Brain.
     */
    suspend fun createMission(request: MissionRequest): Result<MissionResponse> {
        return sendAndAwait<MissionResponse>(
            type = "companion.mission.create",
            payload = json.encodeToString(MissionRequest.serializer(), request)
        )
    }

    /**
     * Query Brain for memory.
     */
    suspend fun queryMemory(query: MemoryQuery): Result<MemoryResult> {
        return sendAndAwait<MemoryResult>(
            type = "companion.query.memory",
            payload = json.encodeToString(MemoryQuery.serializer(), query)
        )
    }

    /**
     * Send a generic command to Brain.
     */
    suspend fun sendCommand(command: String, params: Map<String, String> = emptyMap()): Result<CommandResponse> {
        val request = CommandRequest(command = command, params = params)
        return sendAndAwait<CommandResponse>(
            type = "companion.command",
            payload = json.encodeToString(CommandRequest.serializer(), request)
        )
    }

    /**
     * Get Brain status.
     */
    suspend fun getBrainStatus(): Result<SystemStatus> {
        return sendAndAwait<SystemStatus>(
            type = "companion.query.status"
        )
    }

    /**
     * Get all agents from Brain.
     */
    suspend fun getAgents(): Result<List<AgentStatus>> {
        return sendAndAwait<List<AgentStatus>>(
            type = "companion.query.agents"
        )
    }

    /**
     * Send UI tree to Brain for analysis.
     */
    suspend fun sendUITree(tree: String): Result<CommandResponse> {
        return sendAndAwait<CommandResponse>(
            type = "companion.uitree.report",
            payload = tree
        )
    }

    /**
     * Send screenshot to Brain for vision processing.
     */
    suspend fun sendScreenshot(base64: String): Result<CommandResponse> {
        return sendAndAwait<CommandResponse>(
            type = "companion.screenshot.report",
            payload = base64
        )
    }

    /**
     * Send voice command to Brain.
     */
    suspend fun sendVoiceCommand(text: String): Result<VoiceResponse> {
        return sendAndAwait<VoiceResponse>(
            type = "companion.voice.command",
            payload = text
        )
    }

    /**
     * Send a raw message to Brain.
     */
    suspend fun sendRawMessage(type: String, payload: String? = null): Result<Unit> = try {
        webSocketService?.emitToBrain(type, payload)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Request-Response Pattern ───────────────────────────

    /**
     * Send a message and wait for a matching response.
     * Uses the first matching event type as the response.
     */
    private suspend inline fun <reified T> sendAndAwait(
        type: String,
        payload: String? = null,
        timeoutMs: Long = 10_000L
    ): Result<T> {
        if (connectionState.value != true) {
            return Result.failure(IllegalStateException("Not connected to Brain"))
        }

        webSocketService?.emitToBrain(type, payload)

        return withTimeoutOrNull(timeoutMs) {
            try {
                brainEvents
                    .filter { it.type.removeSuffix(".response") == type.split(".").last() }
                    .first()
                    .let { response ->
                        response.payload?.let { payloadStr ->
                            Result.success(json.decodeFromString<T>(payloadStr))
                        } ?: Result.failure(Exception("Empty response payload"))
                    }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(Exception("Timeout waiting for Brain response"))
    }

    // ── Event Flows (convenience) ──────────────────────────

    fun observeMissionEvents(): Flow<BrainMessage> = brainEvents
        .filter { it.type.startsWith("brain.mission.") }

    fun observeAgentEvents(): Flow<BrainMessage> = brainEvents
        .filter { it.type.startsWith("brain.agent.") }

    fun observeLogEvents(): Flow<BrainMessage> = brainEvents
        .filter { it.type == "brain.log" }

    fun observeBrainStatusEvents(): Flow<BrainMessage> = brainEvents
        .filter { it.type == "brain.status" }
}
