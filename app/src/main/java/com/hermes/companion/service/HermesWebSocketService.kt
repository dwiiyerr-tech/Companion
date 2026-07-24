package com.hermes.companion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.hermes.companion.core.network.BrainMessage
import com.hermes.companion.core.network.CompanionMessage
import com.hermes.companion.core.network.ConnectionConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Foreground service maintaining a persistent WebSocket connection to Hermes Brain.
 *
 * Responsibilities:
 * - Persistent notification showing connection status
 * - Automatic reconnection with exponential backoff
 * - Heartbeat keep-alive (30s interval)
 * - Forwards incoming Brain events to EventBus / SharedFlow
 * - Handles outbound Companion→Brain messages
 * - Starts automatically on app launch
 */
class HermesWebSocketService : Service() {

    companion object {
        private const val TAG = "HermesWS"
        private const val CHANNEL_ID = "hermes_ws_channel"
        private const val NOTIFICATION_ID = 1001
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val BASE_RECONNECT_DELAY_MS = 2_000L
        private const val MAX_RECONNECT_DELAY_MS = 60_000L
        private const val MAX_RECONNECT_ATTEMPTS = 0 // 0 = unlimited

        private const val EXTRA_ACTION = "action"
        private const val EXTRA_HOST = "host"
        private const val EXTRA_PORT = "port"
        private const val EXTRA_AUTH_TOKEN = "auth_token"

        const val ACTION_START = "com.hermes.companion.action.START_WS"
        const val ACTION_STOP = "com.hermes.companion.action.STOP_WS"
        const val ACTION_SEND = "com.hermes.companion.action.SEND_WS"
        const val EXTRA_MESSAGE = "message"
    }

    // ── State ──────────────────────────────────────────────
    private enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
    }

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** All incoming Brain messages are emitted here for any collector. */
    private val _events = MutableSharedFlow<BrainMessage>(
        extraBufferCapacity = 256,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    val events: SharedFlow<BrainMessage> = _events.asSharedFlow()

    private val _lastMessageTime = MutableStateFlow(0L)
    val lastMessageTime: StateFlow<Long> = _lastMessageTime.asStateFlow()

    private var webSocket: WebSocket? = null
    private var config: ConnectionConfig = ConnectionConfig()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isRunning = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null

    // ── Lifecycle ──────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.i(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(EXTRA_ACTION)) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SEND -> {
                val msg = intent.getStringExtra(EXTRA_MESSAGE)
                if (msg != null) sendRawMessage(msg)
                return START_STICKY
            }
            ACTION_START -> {
                val host = intent.getStringExtra(EXTRA_HOST) ?: config.host
                val port = intent.getIntExtra(EXTRA_PORT, config.port)
                val token = intent.getStringExtra(EXTRA_AUTH_TOKEN) ?: config.authToken
                config = config.copy(host = host, port = port, authToken = token)
            }
        }

        if (!isRunning.getAndSet(true)) {
            startForeground(NOTIFICATION_ID, buildNotification("Connecting…"))
            serviceScope.launch { connect() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning.set(false)
        heartbeatJob?.cancel()
        reconnectJob?.cancel()
        serviceScope.cancel()
        webSocket?.close(1000, "Service destroyed")
        webSocket = null
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
    }

    // ── Connection ─────────────────────────────────────────

    private suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.CONNECTING
        updateNotification("Connecting to Brain…")

        val wsUrl = buildWsUrl()
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // no read timeout for WS
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS)

        val requestBuilder = Request.Builder().url(wsUrl)
            .addHeader("User-Agent", "Hermes-Android-Companion/1.0")
        config.authToken?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val listener = object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket opened to $wsUrl")
                webSocket = ws
                reconnectAttempts.set(0)
                _connectionState.value = ConnectionState.CONNECTED
                updateNotification("Connected to Brain")
                startHeartbeat()

                // Send handshake
                val handshake = BrainMessage(
                    type = "handshake",
                    payload = Json.encodeToString(
                        com.hermes.companion.core.network.HandshakePayload.serializer(),
                        com.hermes.companion.core.network.HandshakePayload()
                    ),
                    timestamp = System.currentTimeMillis()
                )
                ws.send(Json.encodeToString(BrainMessage.serializer(), handshake))
            }

            override fun onMessage(ws: WebSocket, text: String) {
                serviceScope.launch {
                    try {
                        val msg = Json.decodeFromString(BrainMessage.serializer(), text)
                        _lastMessageTime.value = System.currentTimeMillis()
                        _events.emit(msg)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse message: ${e.message}")
                    }
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closing: $code / $reason")
                ws.close(code, reason)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.i(TAG, "WebSocket closed: $code / $reason")
                webSocket = null
                _connectionState.value = ConnectionState.RECONNECTING
                updateNotification("Disconnected – reconnecting…")
                scheduleReconnect()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
                webSocket = null
                _connectionState.value = ConnectionState.RECONNECTING
                updateNotification("Connection error – retrying…")
                scheduleReconnect()
            }
        }

        val client = builder.build()
        webSocket = client.newWebSocket(requestBuilder.build(), listener)
    }

    private fun buildWsUrl(): String {
        val scheme = if (config.useSsl) "wss" else "ws"
        return "$scheme://${config.host}:${config.port}"
    }

    // ── Reconnection ───────────────────────────────────────

    private fun scheduleReconnect() {
        if (!isRunning.get()) return
        reconnectJob?.cancel()

        val attempt = reconnectAttempts.incrementAndGet()
        if (MAX_RECONNECT_ATTEMPTS > 0 && attempt > MAX_RECONNECT_ATTEMPTS) {
            Log.w(TAG, "Max reconnect attempts ($MAX_RECONNECT_ATTEMPTS) reached")
            updateNotification("Connection failed – check Brain")
            return
        }

        val delay = (BASE_RECONNECT_DELAY_MS * attempt.coerceAtMost(10))
            .coerceAtMost(MAX_RECONNECT_DELAY_MS)
        Log.i(TAG, "Reconnecting in ${delay}ms (attempt $attempt)")

        reconnectJob = serviceScope.launch {
            delay(delay)
            if (isRunning.get()) connect()
        }
    }

    // ── Heartbeat ──────────────────────────────────────────

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                delay(HEARTBEAT_INTERVAL_MS)
                val hb = BrainMessage(
                    type = "heartbeat",
                    timestamp = System.currentTimeMillis()
                )
                sendRawMessage(Json.encodeToString(BrainMessage.serializer(), hb))
            }
        }
    }

    // ── Outbound ───────────────────────────────────────────

    fun sendMessage(msg: CompanionMessage) {
        sendRawMessage(Json.encodeToString(CompanionMessage.serializer(), msg))
    }

    fun sendRawMessage(text: String) {
        val state = _connectionState.value
        if (state != ConnectionState.CONNECTED) {
            Log.w(TAG, "Cannot send – state is $state")
            return
        }
        serviceScope.launch {
            val sent = webSocket?.send(text) ?: false
            if (!sent) Log.e(TAG, "WebSocket send failed")
        }
    }

    // ── Notification ───────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Hermes Brain Connection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent WebSocket connection to Hermes Brain"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let { PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            ) }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hermes Companion")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    // ── Public API for sending intents ─────────────────────

    /** Convenience: send a BrainMessage over the socket. */
    fun emitToBrain(type: String, payload: String? = null) {
        val msg = BrainMessage(
            type = type,
            payload = payload,
            timestamp = System.currentTimeMillis()
        )
        sendRawMessage(Json.encodeToString(BrainMessage.serializer(), msg))
    }

    // ── Companion (static helpers) ─────────────────────────

    fun startService(context: Context, host: String, port: Int, token: String?) {
        val intent = Intent(context, HermesWebSocketService::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_START)
            putExtra(EXTRA_HOST, host)
            putExtra(EXTRA_PORT, port)
            putExtra(EXTRA_AUTH_TOKEN, token)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
