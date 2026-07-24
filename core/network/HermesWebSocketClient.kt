package com.hermes.companion.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * OkHttp-based WebSocket client for Hermes Brain communication.
 *
 * Features:
 * - Connect/disconnect lifecycle
 * - JSON serialization/deserialization via kotlinx.serialization
 * - 30-second heartbeat keepalive
 * - Exponential backoff auto-reconnect (1s → 2s → 4s → … → 30s cap)
 * - Authentication token support
 * - SharedFlow for incoming messages
 * - StateFlow for connection state
 */
class HermesWebSocketClient {

    // ── Config ─────────────────────────────────────────────────

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)       // long-lived WS
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    // ── State ──────────────────────────────────────────────────

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING, CLOSING
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<HermesMessage>(extraBufferCapacity = 64)
    val messages: SharedFlow<HermesMessage> = _messages.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var currentUrl: String = ""
    private var authToken: String? = null
    private var reconnectAttempt = 0
    private val maxReconnectDelay = 30_000L  // 30 seconds cap

    // ── Public API ─────────────────────────────────────────────

    fun connect(url: String, token: String? = null) {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        currentUrl = url
        authToken = token
        reconnectAttempt = 0
        doConnect(url, token)
    }

    fun disconnect() {
        _connectionState.value = ConnectionState.CLOSING
        stopHeartbeat()
        reconnectJob?.cancel()
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun send(command: CommandType, payload: Map<String, String>? = null) {
        val msg = HermesCommand(
            id = UUID.randomUUID().toString(),
            command = command,
            payload = payload,
            authToken = authToken
        )
        sendRaw(msg)
    }

    fun sendRaw(message: HermesMessage) {
        val ws = webSocket ?: return
        val serialized = try {
            json.encodeToString<HermesMessage>(message)
        } catch (e: Exception) {
            // Fallback: serialize as the concrete type
            when (message) {
                is HermesCommand -> json.encodeToString(message)
                is HermesResponse -> json.encodeToString(message)
                is HermesEvent -> json.encodeToString(message)
            }
        }
        ws.send(serialized)
    }

    // ── Internals ──────────────────────────────────────────────

    private fun doConnect(url: String, token: String?) {
        _connectionState.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url(url)
            .apply {
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                    header("X-Auth-Token", token)
                }
                header("X-Device-Id", android.os.Build.ID)
                header("X-App-Version", "1.0.0")
            }
            .build()

        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.CONNECTED
                reconnectAttempt = 0
                startHeartbeat()
                // Send auth if token provided
                if (!token.isNullOrBlank()) {
                    send(CommandType.AUTH, mapOf("token" to token))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncoming(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                _connectionState.value = ConnectionState.CLOSING
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect("Closed: $code $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleDisconnect("Failure: ${t.message}")
            }
        })
    }

    private fun handleIncoming(raw: String) {
        scope.launch {
            try {
                val message = json.decodeFromString<HermesMessage>(raw)
                _messages.emit(message)
            } catch (e: Exception) {
                // Attempt raw parse as fallback
                try {
                    val event = json.decodeFromString<HermesEvent>(raw)
                    _messages.emit(event)
                } catch (_: Exception) {
                    // Unparseable message — ignore
                }
            }
        }
    }

    private fun handleDisconnect(reason: String) {
        stopHeartbeat()
        webSocket = null
        if (_connectionState.value != ConnectionState.CLOSING) {
            _connectionState.value = ConnectionState.RECONNECTING
            scheduleReconnect()
        } else {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    // ── Heartbeat ──────────────────────────────────────────────

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (true) {
                delay(30_000L) // 30 seconds
                send(CommandType.HEARTBEAT)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    // ── Auto-reconnect (exponential backoff) ───────────────────

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            reconnectAttempt++
            val delayMs = calculateBackoff(reconnectAttempt)
            delay(delayMs)
            doConnect(currentUrl, authToken)
        }
    }

    private fun calculateBackoff(attempt: Int): Long {
        val base = 1_000L // 1 second
        val backoff = base * (1L shl (attempt - 1).coerceAtMost(4))
        return backoff.coerceAtMost(maxReconnectDelay)
    }
}
