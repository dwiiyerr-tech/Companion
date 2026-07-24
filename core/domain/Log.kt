package com.hermes.companion.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "logs")
@Serializable
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val level: LogLevel,
    val source: String,
    val message: String,
    val timestamp: Long,
    val data: Map<String, String>? = null
)

@Serializable
enum class LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}
