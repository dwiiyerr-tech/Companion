package com.hermes.companion.core.domain

/**
 * Local UI-level log entry (distinct from wire-protocol LogEvent).
 * Used by LogsViewModel and LogsScreen for display.
 */
data class LogEntry(
    val id: String = "",
    val level: LogLevel = LogLevel.INFO,
    val tag: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val stackTrace: String? = null
)
