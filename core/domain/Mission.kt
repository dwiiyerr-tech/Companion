package com.hermes.companion.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.hermes.companion.data.local.Converters
import kotlinx.serialization.Serializable

@Entity(tableName = "missions")
@TypeConverters(Converters::class)
@Serializable
data class Mission(
    @PrimaryKey
    val id: String,
    val name: String,
    val goal: String,
    val status: MissionStatus,
    val created_at: Long,
    val started_at: Long? = null,
    val completed_at: Long? = null,
    val agents: List<String> = emptyList(),
    val progress: Float = 0f,
    val error: String? = null
)

@Serializable
enum class MissionStatus {
    PENDING,
    PLANNING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
