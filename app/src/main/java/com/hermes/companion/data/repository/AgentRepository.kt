package com.hermes.companion.data.repository

import com.hermes.companion.core.network.AgentStatus
import com.hermes.companion.core.network.AgentHealthState
import com.hermes.companion.core.network.AgentTask
import kotlinx.coroutines.flow.*

/**
 * Agent status, health, and task history management.
 *
 * Caches agent data locally. Syncs with Hermes Brain via HermesRepository.
 */
class AgentRepository(
    private val hermesRepository: HermesRepository
) {
    // ── In-Memory Cache ────────────────────────────────────

    private val _agents = MutableStateFlow<Map<String, AgentStatus>>(emptyMap())
    val agents: StateFlow<Map<String, AgentStatus>> = _agents.asStateFlow()

    private val _agentTasks = MutableStateFlow<Map<String, List<AgentTask>>>(emptyMap())
    val agentTasks: StateFlow<Map<String, List<AgentTask>>> = _agentTasks.asStateFlow()

    val healthyAgents: StateFlow<List<AgentStatus>> = _agents
        .map { map -> map.values.filter { it.status == AgentHealthState.HEALTHY } }
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    val unhealthyAgents: StateFlow<List<AgentStatus>> = _agents
        .map { map -> map.values.filter {
            it.status != AgentHealthState.HEALTHY && it.status != AgentHealthState.OFFLINE
        }}
        .stateIn(EmptyCoroutineScope, SharingStarted.Eagerly, emptyList())

    private var lastRefreshTime: Long = 0L
    private val refreshCooldownMs = 5_000L

    // ── CRUD ───────────────────────────────────────────────

    /**
     * Fetch all agents from Brain and update cache.
     */
    suspend fun refreshAgents(): Result<List<AgentStatus>> {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < refreshCooldownMs) {
            return Result.success(_agents.value.values.toList())
        }

        return try {
            val result = hermesRepository.getAgents()
            result.fold(
                onSuccess = { agentList ->
                    lastRefreshTime = now
                    val map = agentList.associateBy { it.agentId }
                    _agents.value = map
                    Result.success(agentList)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get agent by ID.
     */
    fun getAgent(agentId: String): AgentStatus? {
        return _agents.value[agentId]
    }

    /**
     * Get all cached agents.
     */
    fun getAllAgents(): List<AgentStatus> {
        return _agents.value.values.sortedBy { it.name }
    }

    /**
     * Update agent status from Brain event.
     */
    fun updateAgentStatus(agentId: String, status: AgentStatus) {
        _agents.value = _agents.value + (agentId to status)
    }

    /**
     * Get tasks for a specific agent.
     */
    fun getAgentTasks(agentId: String): List<AgentTask> {
        return _agentTasks.value[agentId] ?: emptyList()
    }

    /**
     * Add task to agent's history.
     */
    fun addAgentTask(agentId: String, task: AgentTask) {
        val current = _agentTasks.value[agentId].orEmpty()
        _agentTasks.value = _agentTasks.value + (agentId to (current + task).takeLast(100))
    }

    /**
     * Request a refresh of a specific agent from Brain.
     */
    suspend fun requestAgentRefresh(agentId: String): Result<Unit> {
        return hermesRepository.sendCommand(
            "agent.refresh",
            mapOf("agent_id" to agentId)
        ).map { }
    }

    /**
     * Execute an action on a specific agent.
     */
    suspend fun triggerAgentAction(agentId: String, action: String): Result<Unit> {
        return hermesRepository.sendCommand(
            "agent.action",
            mapOf("agent_id" to agentId, "action" to action)
        ).map { }
    }

    /**
     * Get average CPU across all agents.
     */
    fun getAverageCpu(): Float {
        val agentList = _agents.value.values.toList()
        if (agentList.isEmpty()) return 0f
        return agentList.map { it.cpuPercent }.average().toFloat()
    }

    /**
     * Get total RAM used across all agents (MB).
     */
    fun getTotalRamMb(): Float {
        return _agents.value.values.sumOf { it.ramMb.toDouble() }.toFloat()
    }

    /**
     * Get agent count by health state.
     */
    fun getCountByHealth(state: AgentHealthState): Int {
        return _agents.value.values.count { it.status == state }
    }

    /**
     * Clear agent cache.
     */
    fun clearCache() {
        _agents.value = emptyMap()
        _agentTasks.value = emptyMap()
        lastRefreshTime = 0L
    }
}
