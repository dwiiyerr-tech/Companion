package com.hermes.companion.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hermes.companion.data.local.Converters
import kotlinx.serialization.Serializable

@Entity(tableName = "agents")
@TypeConverters(Converters::class)
@Serializable
data class Agent(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: AgentType,
    val status: AgentStatus,
    val health: Float = 1f,
    val cpu: Float = 0f,
    val ram: Float = 0f,
    val current_task: String? = null,
    val history: List<AgentHistoryEntry> = emptyList()
)

@Serializable
enum class AgentType {
    CODING,
    BROWSER,
    FILESYSTEM,
    TERMINAL,
    VOICE,
    VISION,
    COMPOSITE
}

@Serializable
enum class AgentStatus {
    IDLE,
    WORKING,
    WAITING,
    ERROR,
    STOPPED,
    STARTING,
    STOPPING
}

@Serializable
data class AgentHistoryEntry(
    val task: String,
    val started_at: Long,
    val completed_at: Long? = null,
    val success: Boolean? = null,
    val summary: String? = null
)
