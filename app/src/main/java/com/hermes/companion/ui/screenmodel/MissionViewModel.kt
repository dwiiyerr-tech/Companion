package com.hermes.companion.ui.screenmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

/**
 * Mission execution state management.
 *
 * Tracks running missions, mission history, queues, and mission progress.
 */
class MissionViewModel : ViewModel() {

    // ── Unified UI State ───────────────────────────────────

    private val _uiState = MutableStateFlow(MissionScreenUiState())
    val uiState: StateFlow<MissionScreenUiState> = _uiState.asStateFlow()

    var showCreateMissionDialog by mutableStateOf(false)
        private set

    fun selectMission(mission: MissionUi) {
        _uiState.update { it.copy(selectedMission = mission) }
    }

    fun dismissMissionDialog() {
        showCreateMissionDialog = false
    }

    fun dismissSelectedMission() {
        _uiState.update { it.copy(selectedMission = null) }
    }

    // ── Mission State ─────────────────────────────────────

    private val _currentMissionId = MutableStateFlow<String?>(null)
    val currentMissionId: StateFlow<String?> = _currentMissionId.asStateFlow()

    private val _missionState = MutableStateFlow<MissionState>(MissionState.IDLE)
    val missionState: StateFlow<MissionState> = _missionState.asStateFlow()

    private val _missionProgress = MutableStateFlow(0f)
    val missionProgress: StateFlow<Float> = _missionProgress.asStateFlow()

    private val _missionStep = MutableStateFlow<String?>(null)
    val missionStep: StateFlow<String?> = _missionStep.asStateFlow()

    private val _missionMessage = MutableStateFlow<String?>(null)
    val missionMessage: StateFlow<String?> = _missionMessage.asStateFlow()

    private val _missionError = MutableStateFlow<String?>(null)
    val missionError: StateFlow<String?> = _missionError.asStateFlow()

    // ── Mission Lists ──────────────────────────────────────

    private val _runningMissions = MutableStateFlow<List<MissionUi>>(emptyList())
    val runningMissions: StateFlow<List<MissionUi>> = _runningMissions.asStateFlow()

    private val _missionHistory = MutableStateFlow<List<MissionUi>>(emptyList())
    val missionHistory: StateFlow<List<MissionUi>> = _missionHistory.asStateFlow()

    private val _missionQueues = MutableStateFlow<List<MissionQueue>>(emptyList())
    val missionQueues: StateFlow<List<MissionQueue>> = _missionQueues.asStateFlow()

    data class MissionScreenUiState(
        val runningMissions: List<MissionUi> = emptyList(),
        val queuedMissions: List<MissionUi> = emptyList(),
        val completedMissions: List<MissionUi> = emptyList(),
        val failedMissions: List<MissionUi> = emptyList(),
        val showCreateMissionDialog: Boolean = false,
        val selectedMission: MissionUi? = null
    )

    data class MissionUi(
        val id: String,
        val name: String,
        val status: MissionStatus,
        val progress: Float,
        val step: String?,
        val message: String?,
        val createdAt: Long,
        val startedAt: Long? = null,
        val completedAt: Long? = null,
        val agentTypes: List<String>
    )

    data class MissionQueue(
        val name: String,
        val priority: Int,
        val count: Int,
        val estimatedDuration: Long
    )

    // ── State Management ───────────────────────────────────

    fun setCurrentMission(missionId: String) {
        _currentMissionId.value = missionId
    }

    fun startMission(name: String, goals: List<String>, agentTypes: List<String>, priority: Int) {
        _currentMissionId.value = "temp_${System.currentTimeMillis()}"
        _missionState.value = MissionState.STARTING
        _missionProgress.value = 0f
        _missionStep.value = "Preparing mission: $name"
        _missionMessage.value = "Mission '$name' started"
        _missionError.value = null

        // TODO: Call missionRepository.createMission when ready
        // This would integrate with the Hermes repository when implemented
    }

    fun updateMissionStatus(
        status: MissionState,
        progress: Float,
        step: String? = null,
        message: String? = null
    ) {
        _missionState.value = status
        _missionProgress.value = progress
        if (step != null) _missionStep.value = step
        if (message != null) _missionMessage.value = message

        if (status == MissionState.COMPLETED) {
            _missionStep.value = "Mission completed"
        } else if (status == MissionState.FAILED || status == MissionState.CANCELLED) {
            _missionStep.value = "Mission failed"
            _missionError.value = message
        } else if (status == MissionState.RUNNING && progress > 0f) {
            _missionError.value = null
        }
    }

    fun completeMission() {
        updateMissionStatus(MissionState.COMPLETED, 1f, "Completed", "Mission finished successfully")
        _missionState.value = MissionState.COMPLETED
        _currentMissionId.value = null
    }

    fun errorMission(error: String) {
        updateMissionStatus(MissionState.FAILED, 0f, null, error)
    }

    fun cancelMission() {
        updateMissionStatus(MissionState.CANCELLED, 0f, "Cancelled", "Mission cancelled by user")
        _currentMissionId.value = null
    }

    fun resetMission() {
        _missionState.value = MissionState.IDLE
        _missionProgress.value = 0f
        _missionStep.value = null
        _missionMessage.value = null
        _missionError.value = null
        _currentMissionId.value = null
    }

    // ── Mission Queue Management ────────────────────────────

    fun updateQueues(queues: List<MissionQueue>) {
        _missionQueues.value = queues
    }

    fun updateRunningMissions(missions: List<MissionUi>) {
        _runningMissions.value = missions
    }

    fun updateMissionHistory(missions: List<MissionUi>) {
        _missionHistory.value = missions
    }

    // ── External Actions ───────────────────────────────────

    fun pauseCurrentMission() {
        if (_currentMissionId.value != null) {
            // TODO: Call missionRepository.pauseMission
            updateMissionStatus(MissionState.PAUSED, _missionProgress.value, "Paused", "Mission paused")
        }
    }

    fun resumeCurrentMission() {
        if (_currentMissionId.value != null) {
            // TODO: Call missionRepository.resumeMission
            updateMissionStatus(MissionState.RUNNING, _missionProgress.value, "Resumed", "Mission resumed")
        }
    }

    fun cancelCurrentMission() {
        if (_currentMissionId.value != null) {
            // TODO: Call missionRepository.cancelMission
            cancelMission()
        }
    }

    // ── Mission Details ─────────────────────────────────────

    fun getMissionDetails(missionId: String): MissionUi? {
        return _runningMissions.value.find { it.id == missionId } 
            ?: _missionHistory.value.find { it.id == missionId }
    }

    fun getActiveMission(): MissionUi? {
        return _runningMissions.value.find { it.status == MissionStatus.RUNNING }
    }

    fun hasActiveMission(): Boolean {
        return _runningMissions.value.any { it.status == MissionStatus.RUNNING }
    }

    fun getActiveMissionCount(): Int {
        return _runningMissions.value.count { it.status == MissionStatus.RUNNING || it.status == MissionStatus.QUEUED }
    }
}

// ── Helper Enums ──────────────────────────────────────

enum class MissionState {
    IDLE, STARTING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

enum class MissionStatus {
    QUEUED, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}
