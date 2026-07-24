package com.hermes.companion.data.repository

import com.hermes.companion.core.network.MemoryEntry
import com.hermes.companion.core.network.MemoryQuery
import com.hermes.companion.core.network.MemoryResult
import com.hermes.companion.core.network.MemoryType
import kotlinx.coroutines.flow.*

/**
 * Memory CRUD with short-term / long-term / semantic storage.
 *
 * Provides local cache with search operations. Syncs with Hermes Brain.
 */
class MemoryRepository(
    private val hermesRepository: HermesRepository
) {
    // ── In-Memory Cache (Room-ready) ───────────────────────

    private val _memories = MutableStateFlow<Map<String, MemoryEntry>>(emptyMap())
    val memories: StateFlow<Map<String, MemoryEntry>> = _memories.asStateFlow()

    val shortTermMemories: StateFlow<List<MemoryEntry>> = _memories
        .map { map -> map.values.filter { it.type == MemoryType.SHORT_TERM } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val longTermMemories: StateFlow<List<MemoryEntry>> = _memories
        .map { map -> map.values.filter { it.type == MemoryType.LONG_TERM } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val semanticMemories: StateFlow<List<MemoryEntry>> = _memories
        .map { map -> map.values.filter { it.type == MemoryType.SEMANTIC } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val memoryStats: StateFlow<MemoryStats> = _memories
        .map { map ->
            MemoryStats(
                total = map.size,
                shortTerm = map.values.count { it.type == MemoryType.SHORT_TERM },
                longTerm = map.values.count { it.type == MemoryType.LONG_TERM },
                semantic = map.values.count { it.type == MemoryType.SEMANTIC },
                episodic = map.values.count { it.type == MemoryType.EPISODIC },
                procedural = map.values.count { it.type == MemoryType.PROCEDURAL }
            )
        }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, MemoryStats())

    data class MemoryStats(
        val total: Int = 0,
        val shortTerm: Int = 0,
        val longTerm: Int = 0,
        val semantic: Int = 0,
        val episodic: Int = 0,
        val procedural: Int = 0
    )

    // ── CRUD ───────────────────────────────────────────────

    /**
     * Query memories from Brain.
     */
    suspend fun queryMemories(query: MemoryQuery): Result<MemoryResult> {
        return try {
            val result = hermesRepository.queryMemory(query)
            result.fold(
                onSuccess = { memResult ->
                    // Cache results locally
                    val map = memResult.entries.associateBy { it.id }
                    _memories.value = _memories.value + map
                    Result.success(memResult)
                },
                onFailure = { e ->
                    // Offline fallback: search local cache
                    Result.success(searchLocal(query))
                }
            )
        } catch (e: Exception) {
            Result.success(searchLocal(query))
        }
    }

    /**
     * Get a specific memory by ID.
     */
    fun getMemory(memoryId: String): MemoryEntry? {
        return _memories.value[memoryId]
    }

    /**
     * Get all memories, sorted by most recent.
     */
    fun getAllMemories(): List<MemoryEntry> {
        return _memories.value.values.sortedByDescending { it.createdAt }
    }

    /**
     * Add a memory to local cache.
     */
    fun addMemory(memory: MemoryEntry) {
        _memories.value = _memories.value + (memory.id to memory)
    }

    /**
     * Delete a memory from local cache.
     */
    fun deleteMemory(memoryId: String) {
        _memories.value = _memories.value - memoryId
    }

    /**
     * Clear all local memories.
     */
    fun clearAll() {
        _memories.value = emptyMap()
    }

    // ── Search ─────────────────────────────────────────────

    /**
     * Full-text search on locally cached memories.
     */
    fun searchLocal(query: String, type: MemoryType? = null): MemoryResult {
        val results = _memories.value.values.filter { entry ->
            val matchesQuery = query.isBlank() ||
                entry.content.contains(query, ignoreCase = true) ||
                entry.tags.any { it.contains(query, ignoreCase = true) }
            val matchesType = type == null || entry.type == type
            matchesQuery && matchesType
        }.sortedByDescending { it.importance }

        return MemoryResult(
            entries = results,
            total = results.size,
            query = query
        )
    }

    /**
     * Search by tag.
     */
    fun searchByTag(tag: String): List<MemoryEntry> {
        return _memories.value.values
            .filter { tag in it.tags }
            .sortedByDescending { it.importance }
    }

    /**
     * Get memories by type.
     */
    fun getByType(type: MemoryType): List<MemoryEntry> {
        return _memories.value.values
            .filter { it.type == type }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Get most important memories.
     */
    fun getMostImportant(limit: Int = 10): List<MemoryEntry> {
        return _memories.value.values
            .sortedByDescending { it.importance }
            .take(limit)
    }

    /**
     * Get recently accessed memories.
     */
    fun getRecentlyAccessed(limit: Int = 10): List<MemoryEntry> {
        return _memories.value.values
            .filter { it.accessedAt != null }
            .sortedByDescending { it.accessedAt }
            .take(limit)
    }

    /**
     * Create a new memory on Brain.
     */
    suspend fun createMemory(
        content: String,
        type: MemoryType = MemoryType.LONG_TERM,
        tags: List<String> = emptyList(),
        importance: Float = 0.5f,
        source: String? = null
    ): Result<String> {
        return hermesRepository.sendCommand(
            "memory.create",
            mapOf(
                "content" to content,
                "type" to type.name,
                "tags" to tags.joinToString(","),
                "importance" to importance.toString(),
                "source" to (source ?: "companion")
            )
        ).map { it.data ?: "" }
    }

    /**
     * Update a memory on Brain.
     */
    suspend fun updateMemory(
        memoryId: String,
        content: String? = null,
        tags: List<String>? = null,
        importance: Float? = null
    ): Result<Unit> {
        val params = mutableMapOf("memory_id" to memoryId)
        content?.let { params["content"] = it }
        tags?.let { params["tags"] = it.joinToString(",") }
        importance?.let { params["importance"] = it.toString() }

        return hermesRepository.sendCommand("memory.update", params).map { }
    }

    /**
     * Delete a memory from Brain.
     */
    suspend fun deleteMemoryFromBrain(memoryId: String): Result<Unit> {
        deleteMemory(memoryId)
        return hermesRepository.sendCommand(
            "memory.delete",
            mapOf("memory_id" to memoryId)
        ).map { }
    }
}
