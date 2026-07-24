package com.hermes.companion.core.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "memory")
@Serializable
data class Memory(
    @PrimaryKey
    val id: String,
    val type: MemoryType,
    val content: String,
    val embedding: List<Float>? = null,
    val created_at: Long,
    val metadata: Map<String, String>? = null
)

@Serializable
enum class MemoryType {
    EPISODIC,
    SEMANTIC,
    PROCEDURAL,
    SYSTEM
}
