package com.hermes.companion.ui.screenmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

/**
 * Voice interaction state management.
 *
 * Tracks listening, processing, speaking states and maintains conversation history.
 */
class VoiceViewModel : ViewModel() {

    // ── Voice Mode ─────────────────────────────────────────

    enum class VoiceMode { PUSH_TO_TALK, HANDS_FREE }

    private val _voiceMode = MutableStateFlow(VoiceMode.PUSH_TO_TALK)
    val voiceMode: StateFlow<VoiceMode> = _voiceMode.asStateFlow()

    // ── Voice State ────────────────────────────────────────

    enum class VoiceState {
        IDLE,           // Not listening
        LISTENING,      // Actively listening
        PROCESSING,     // Recognizing speech
        SPEAKING,       // TTS output
        WAKE_WORD,      // Wake word detected
        ERROR           // Error occurred
    }

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _currentTranscription = MutableStateFlow("")
    val currentTranscription: StateFlow<String> = _currentTranscription.asStateFlow()

    private val _partialTranscription = MutableStateFlow("")
    val partialTranscription: StateFlow<String> = _partialTranscription.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── Conversation History ───────────────────────────────

    data class ConversationEntry(
        val role: String,    // "user", "assistant", "system"
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val isWakeWord: Boolean = false
    )

    private val _conversationHistory = MutableStateFlow<List<ConversationEntry>>(emptyList())
    val conversationHistory: StateFlow<List<ConversationEntry>> = _conversationHistory.asStateFlow()

    private val maxHistorySize = 100

    // ── TTS State ──────────────────────────────────────────

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    // ── State Management ───────────────────────────────────

    fun setListening() {
        _voiceState.value = VoiceState.LISTENING
        _error.value = null
    }

    fun setProcessing() {
        _voiceState.value = VoiceState.PROCESSING
    }

    fun setSpeaking() {
        _voiceState.value = VoiceState.SPEAKING
    }

    fun setIdle() {
        _voiceState.value = VoiceState.IDLE
        _currentTranscription.value = ""
    }

    fun setWakeWordDetected() {
        _voiceState.value = VoiceState.WAKE_WORD
    }

    fun setError(message: String) {
        _voiceState.value = VoiceState.ERROR
        _error.value = message
    }

    fun clearError() {
        _error.value = null
        if (_voiceState.value == VoiceState.ERROR) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    // ── Transcription ──────────────────────────────────────

    fun setCurrentTranscription(text: String) {
        _currentTranscription.value = text
    }

    fun setPartialTranscription(text: String) {
        _partialTranscription.value = text
    }

    // ── Conversation ───────────────────────────────────────

    fun addUserMessage(text: String, isWakeWord: Boolean = false) {
        val entry = ConversationEntry(
            role = "user",
            text = text,
            isWakeWord = isWakeWord
        )
        addToHistory(entry)
        _currentTranscription.value = text
    }

    fun addAssistantMessage(text: String) {
        val entry = ConversationEntry(role = "assistant", text = text)
        addToHistory(entry)
    }

    fun addSystemMessage(text: String) {
        val entry = ConversationEntry(role = "system", text = text)
        addToHistory(entry)
    }

    private fun addToHistory(entry: ConversationEntry) {
        val current = _conversationHistory.value.toMutableList()
        current.add(entry)
        // Trim old entries
        while (current.size > maxHistorySize) {
            current.removeFirst()
        }
        _conversationHistory.value = current
    }

    fun clearHistory() {
        _conversationHistory.value = emptyList()
    }

    // ── Mode ───────────────────────────────────────────────

    fun setVoiceMode(mode: VoiceMode) {
        _voiceMode.value = mode
    }

    fun toggleMode() {
        _voiceMode.value = when (_voiceMode.value) {
            VoiceMode.PUSH_TO_TALK -> VoiceMode.HANDS_FREE
            VoiceMode.HANDS_FREE -> VoiceMode.PUSH_TO_TALK
        }
    }

    // ── TTS ────────────────────────────────────────────────

    fun setTtsReady(ready: Boolean) {
        _ttsReady.value = ready
    }
}
