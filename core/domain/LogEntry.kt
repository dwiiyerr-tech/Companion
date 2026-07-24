package com.hermes.companion.core.domain

import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val id: String = "",
    val level: LogLevel = LogLevel.INFO,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String = "",
    val message: String = "",
    val data: Map<String, String> = emptyMap()
) {
    // Helper constructor for UI (maintains backward compatibility)
    constructor(
        id: String,
        level: LogLevel,
        timestamp: Long,
        source: String,
        message: String
    ) : this(id, level, timestamp, source, message, emptyMap())
}