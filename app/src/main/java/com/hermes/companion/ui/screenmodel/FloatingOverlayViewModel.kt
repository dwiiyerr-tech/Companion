package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import com.hermes.companion.service.OverlayService
import kotlinx.coroutines.flow.*

/**
 * State management for the floating overlay.
 *
 * Manages mode transitions, voice state, and mission progress display
 * for the OverlayService.
 */
class FloatingOverlayViewModel : ViewModel() {

    // ── Overlay Mode ───────────────────────────────────────

    private val _overlayMode = MutableStateFlow(OverlayService.OverlayMode.BUBBLE)
    val overlayMode: StateFlow<OverlayService.OverlayMode> = _overlayMode.asStateFlow()

    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    // ── Voice State ────────────────────────────────────────

    enum class VoiceUIState { IDLE, LISTENING, PROCESSING, SPEAKING, ERROR }

    private val _voiceState = MutableStateFlow(VoiceUIState.IDLE)
    val voiceState: StateFlow<VoiceUIState> = _voiceState.asStateFlow()

    private val _lastVoiceText = MutableStateFlow("")
    val lastVoiceText: StateFlow<String> = _lastVoiceText.asStateFlow()

    // ── Mission Progress ───────────────────────────────────

    private val _missionName = MutableStateFlow<String?>(null)
    val missionName: StateFlow<String?> = _missionName.asStateFlow()

    private val _missionProgress = MutableStateFlow(0f)
    val missionProgress: StateFlow<Float> = _missionProgress.asStateFlow()

    private val _missionStep = MutableStateFlow<String?>(null)
    val missionStep: StateFlow<String?> = _missionStep.asStateFlow()

    private val _missionRunning = MutableStateFlow(false)
    val missionRunning: StateFlow<Boolean> = _missionRunning.asStateFlow()

    // ── Brain Connection ───────────────────────────────────

    private val _brainConnected = MutableStateFlow(false)
    val brainConnected: StateFlow<Boolean> = _brainConnected.asStateFlow()

    private val _brainLoad = MutableStateFlow(0f)
    val brainLoad: StateFlow<Float> = _brainLoad.asStateFlow()

    // ── Error State ────────────────────────────────────────

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Mode Transitions ───────────────────────────────────

    fun cycleMode() {
        _overlayMode.value = when (_overlayMode.value) {
            OverlayService.OverlayMode.BUBBLE -> OverlayService.OverlayMode.COMPACT
            OverlayService.OverlayMode.COMPACT -> OverlayService.OverlayMode.EXPANDED
            OverlayService.OverlayMode.EXPANDED -> OverlayService.OverlayMode.BUBBLE
        }
    }

    fun setMode(mode: OverlayService.OverlayMode) {
        _overlayMode.value = mode
    }

    fun show() {
        _isVisible.value = true
    }

    fun hide() {
        _isVisible.value = false
    }

    // ── Voice ──────────────────────────────────────────────

    fun setVoiceState(state: VoiceUIState) {
        _voiceState.value = state
    }

    fun setVoiceText(text: String) {
        _lastVoiceText.value = text
    }

    // ── Mission ────────────────────────────────────────────

    fun setMissionProgress(name: String?, progress: Float, step: String?) {
        _missionName.value = name
        _missionProgress.value = progress
        _missionStep.value = step
        _missionRunning.value = progress > 0f && progress < 1f
    }

    fun clearMission() {
        _missionName.value = null
        _missionProgress.value = 0f
        _missionStep.value = null
        _missionRunning.value = false
    }

    // ── Brain ──────────────────────────────────────────────

    fun setBrainConnected(connected: Boolean) {
        _brainConnected.value = connected
    }

    fun setBrainLoad(load: Float) {
        _brainLoad.value = load
    }

    // ── Error ──────────────────────────────────────────────

    fun setError(error: String?) {
        _error.value = error
    }

    fun clearError() {
        _error.value = null
    }
}
