package com.hermes.companion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * NotificationListenerService that monitors all Android notifications.
 *
 * Responsibilities:
 * - Monitor all incoming/outgoing notifications
 * - Filter by Hermes-relevant apps (configurable)
 * - Store recent notification history (in-memory ring buffer)
 * - Forward notification events to EventBus / SharedFlow
 * - Support reply actions for inline replies
 */
class HermesNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "HermesNotif"
        private const val CHANNEL_ID = "hermes_notif_channel"
        private const val MAX_HISTORY_SIZE = 200

        /** Static reference */
        @Volatile
        var instance: HermesNotificationService? = null
            private set

        @Volatile
        var isRunning: Boolean = false
            private set

        /** Default relevant packages to monitor (extendable via settings) */
        val RELEVANT_PACKAGES = mutableSetOf(
            "com.hermes.companion",
            "com.whatsapp",
            "com.google.android.gm",
            "com.slack",
            "com.telegram.messenger",
            "com.discord",
            "com.microsoft.teams",
            "org.mozilla.firefox",
            "com.android.chrome"
        )
    }

    // ── Notification Data ──────────────────────────────────

    data class NotifEvent(
        val packageName: String,
        val title: String?,
        val body: String?,
        val key: String,
        val timestamp: Long,
        val isRemoved: Boolean = false,
        val canReply: Boolean = false
    )

    private val _notifEvents = MutableSharedFlow<NotifEvent>(
        extraBufferCapacity = 128,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    val notifEvents: SharedFlow<NotifEvent> = _notifEvents.asSharedFlow()

    private val _notifHistory = MutableStateFlow<List<NotifEvent>>(emptyList())
    val notifHistory: StateFlow<List<NotifEvent>> = _notifHistory.asStateFlow()

    private val historyRing = ConcurrentLinkedDeque<NotifEvent>()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ── Lifecycle ──────────────────────────────────────────

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        isRunning = true
        createNotificationChannel()
        Log.i(TAG, "Notification listener connected")
        loadExistingNotifications()
    }

    override fun onListenerDisconnected() {
        instance = null
        isRunning = false
        serviceScope.cancel()
        super.onListenerDisconnected()
        Log.i(TAG, "Notification listener disconnected")
    }

    // ── Notification Events ────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence("android.title")?.toString()
        val body = extras.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName

        val canReply = try {
            val actions = notification.actions
            actions?.any { it?.getRemoteInputs()?.isNotEmpty() == true } ?: false
        } catch (e: Exception) { false }

        val event = NotifEvent(
            packageName = packageName,
            title = title,
            body = body,
            key = sbn.key,
            timestamp = sbn.postTime,
            canReply = canReply
        )

        addToHistory(event)

        serviceScope.launch {
            _notifEvents.emit(event)
        }

        Log.d(TAG, "Notification: [$packageName] $title - $body")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val event = NotifEvent(
            packageName = sbn.packageName,
            title = null,
            body = null,
            key = sbn.key,
            timestamp = System.currentTimeMillis(),
            isRemoved = true
        )

        serviceScope.launch {
            _notifEvents.emit(event)
        }
    }

    // ── History Management ─────────────────────────────────

    private fun addToHistory(event: NotifEvent) {
        if (event.isRemoved) return
        historyRing.addFirst(event)
        while (historyRing.size > MAX_HISTORY_SIZE) {
            historyRing.removeLast()
        }
        _notifHistory.value = historyRing.toList()
    }

    private fun loadExistingNotifications() {
        val active = try {
            activeNotifications
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load active notifications: ${e.message}")
            return
        }

        active?.forEach { sbn ->
            onNotificationPosted(sbn)
        }
    }

    /**
     * Returns filtered history matching the given set of packages.
     * Pass empty set to return all.
     */
    fun getHistory(filterPackages: Set<String> = emptySet()): List<NotifEvent> {
        val history = _notifHistory.value
        return if (filterPackages.isEmpty()) {
            history
        } else {
            history.filter { it.packageName in filterPackages }
        }
    }

    // ── Reply Action ───────────────────────────────────────

    /**
     * Send a reply to a notification that supports inline reply.
     */
    fun replyToNotification(sbn: StatusBarNotification, replyText: String) {
        val notification = sbn.notification ?: return
        val actions = notification.actions ?: return

        for (action in actions) {
            action ?: continue
            val remoteInputs = action.getRemoteInputs() ?: continue

            val intent = Intent().apply {
                for (ri in remoteInputs) {
                    val resultData = Bundle().apply {
                        putCharSequence(ri.resultKey, replyText)
                    }
                    addResultExtras(resultData)
                }
            }

            try {
                action.sendWithResult(this, 0, intent)
                Log.i(TAG, "Reply sent to ${sbn.packageName}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send reply: ${e.message}")
            }
            return
        }
    }

    // ── Package Filter ─────────────────────────────────────

    fun addRelevantPackage(packageName: String) {
        RELEVANT_PACKAGES.add(packageName)
    }

    fun removeRelevantPackage(packageName: String) {
        RELEVANT_PACKAGES.remove(packageName)
    }

    fun isRelevantPackage(packageName: String): Boolean {
        return packageName in RELEVANT_PACKAGES || packageName == packageName
    }

    // ── Notification Channel ───────────────────────────────

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification listener status"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
