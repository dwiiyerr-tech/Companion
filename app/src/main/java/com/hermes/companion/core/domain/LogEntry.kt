package com.hermes.companion.core.domain

/**
 * Local UI-level log entry (distinct from wire-protocol LogEvent).
 * Used by LogsViewModel, LogsScreen, LogStream, and BrowserScreen for display.
 *
 * Field order matches constructor calls:
 *   LogEntry("l1", LogLevel.INFO, timestamp, "Source", "message text")
 */
data class LogEntry(
    val id: String = "",
    val level: LogLevel = LogLevel.INFO,
    val timestamp: Long = 0L,
    val source: String = "",
    val message: String = "",
    val stackTrace: String? = null
)