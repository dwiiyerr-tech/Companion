package com.hermes.companion.data.repository

import com.hermes.companion.core.network.MissionRequest
import com.hermes.companion.core.network.MissionResponse
import com.hermes.companion.core.network.MissionState
import kotlinx.coroutines.flow.*
import java.util.UUID

/**
 * Mission CRUD + status tracking.
 *
 * Provides local persistence with in-memory cache (Room-ready).
 * Syncs with Hermes Brain via HermesRepository.
 *
 * Uses CachedMission (renamed from inner Mission) to avoid
 * collision with core.domain.Mission Room entity.
 */
class MissionRepository(
    private val hermesRepository: HermesRepository
) {
    // ── In-Memory Cache (Room-ready interface) ─────────────

    data class CachedMission(
        val id: String,
        val name: String,
        val goals: List<String>,
        val state: MissionState,
        val progress: Float = 0f,
        val step: String? = null,
        val message: String? = null,
        val agentTypes: List<String> = emptyList(),
        val createdAt: Long,
        val startedAt: Long? = null,
        val completedAt: Long? = null,
        val error: String? = null
    )

    private val _missions = MutableStateFlow<Map<String, CachedMission>>(emptyMap())
    val missions: StateFlow<Map<String, CachedMission>> = _missions.asStateFlow()

    val runningMissions: StateFlow<List<CachedMission>> = _missions
        .map { map -> map.values.filter { it.state == MissionState.RUNNING || it.state == MissionState.QUEUED } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val completedMissions: StateFlow<List<CachedMission>> = _missions
        .map { map -> map.values.filter { it.state == MissionState.COMPLETED } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val failedMissions: StateFlow<List<CachedMission>> = _missions
        .map { map -> map.values.filter { it.state == MissionState.FAILED } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    // ── CRUD ───────────────────────────────────────────────

    /**
     * Create a new mission and sync with Brain.
     */
    suspend fun createMission(request: MissionRequest): Result<String> {
        val tempId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // Optimistic local insert
        val mission = CachedMission(
            id = tempId,
            name = request.name,
            goals = request.goals,
            state = MissionState.QUEUED,
            agentTypes = request.agentTypes,
            createdAt = now
        )
        _missions.value = _missions.value + (tempId to mission)

        return try {
            val response = hermesRepository.createMission(request)
            response.fold(
                onSuccess = { resp ->
                    // Replace temp ID with Brain-assigned ID
                    if (resp.missionId != tempId) {
                        val updated = mission.copy(id = resp.missionId)
                        _missions.value = _missions.value - tempId + (resp.missionId to updated)
                    }
                    Result.success(resp.missionId)
                },
                onFailure = { e ->
                    _missions.value = _missions.value - tempId
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            _missions.value = _missions.value - tempId
            Result.failure(e)
        }
    }

    /**
     * Get mission by ID (local cache first, then Brain).
     */
    suspend fun getMission(missionId: String): CachedMission? {
        // Local cache hit
        _missions.value[missionId]?.let { return it }

        // Fetch from Brain
        try {
            val result = hermesRepository.sendCommand(
                "mission.get",
                mapOf("mission_id" to missionId)
            )
            result.getOrNull()?.let { response ->
                response.data?.let { data ->
                    val parsed = kotlinx.serialization.json.Json.decodeFromString<MissionResponse>(data)
                    val mission = CachedMission(
                        id = parsed.missionId,
                        name = missionId,
                        goals = emptyList(),
                        state = parsed.status.state,
                        createdAt = parsed.createdAt
                    )
                    _missions.value = _missions.value + (mission.id to mission)
                    return mission
                }
            }
        } catch (e: Exception) {
            // Fall back to null
        }
        return null
    }

    /**
     * Get all missions.
     */
    fun getAllMissions(): List<CachedMission> {
        return _missions.value.values.sortedByDescending { it.createdAt }
    }

    /**
     * Update mission status from Brain events.
     */
    fun updateMissionStatus(
        missionId: String,
        state: MissionState? = null,
        progress: Float? = null,
        step: String? = null,
        message: String? = null,
        error: String? = null
    ) {
        val existing = _missions.value[missionId] ?: return
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            state = state ?: existing.state,
            progress = progress ?: existing.progress,
            step = step ?: existing.step,
            message = message ?: existing.message,
            error = error ?: existing.error,
            startedAt = if (state == MissionState.RUNNING && existing.startedAt == null) now else existing.startedAt,
            completedAt = if (state == MissionState.COMPLETED || state == MissionState.FAILED) now else existing.completedAt
        )
        _missions.value = _missions.value + (missionId to updated)
    }

    /**
     * Pause a running mission.
     */
    suspend fun pauseMission(missionId: String): Result<Unit> {
        updateMissionStatus(missionId, state = MissionState.PAUSED)
        return hermesRepository.sendCommand("mission.pause", mapOf("mission_id" to missionId))
            .map { }
    }

    /**
     * Resume a paused mission.
     */
    suspend fun resumeMission(missionId: String): Result<Unit> {
        updateMissionStatus(missionId, state = MissionState.RUNNING)
        return hermesRepository.sendCommand("mission.resume", mapOf("mission_id" to missionId))
            .map { }
    }

    /**
     * Cancel a mission.
     */
    suspend fun cancelMission(missionId: String): Result<Unit> {
        updateMissionStatus(missionId, state = MissionState.CANCELLED)
        return hermesRepository.sendCommand("mission.cancel", mapOf("mission_id" to missionId))
            .map { }
    }

    /**
     * Delete a mission from local cache.
     */
    fun deleteMission(missionId: String) {
        _missions.value = _missions.value - missionId
    }

    /**
     * Clear all missions.
     */
    fun clearAll() {
        _missions.value = emptyMap()
    }

    /**
     * Get mission count by state.
     */
    fun getCountByState(state: MissionState): Int {
        return _missions.value.values.count { it.state == state }
    }
}
