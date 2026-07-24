package com.hermes.companion.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Locale

/**
 * Voice interface service.
 *
 * Responsibilities:
 * - Wake word detection ("Hey Hermes" / "Hermes")
 * - Speech-to-text via Android SpeechRecognizer
 * - Text-to-speech output via Android TTS
 * - Push-to-talk mode (hold button, release to send)
 * - Hands-free mode (continuous listening)
 * - Routes voice commands to Hermes Brain via EventBus
 */
class VoiceService : Service() {

    companion object {
        private const val TAG = "VoiceService"
        private const val WAKE_WORD = "hey hermes"
        private const val WAKE_WORD_SHORT = "hermes"

        const val ACTION_START_LISTENING = "com.hermes.companion.action.START_LISTENING"
        const val ACTION_STOP_LISTENING = "com.hermes.companion.action.STOP_LISTENING"
        const val ACTION_SET_MODE = "com.hermes.companion.action.SET_VOICE_MODE"
        const val ACTION_SPEAK = "com.hermes.companion.action.SPEAK"
        const val EXTRA_TEXT = "text"
        const val EXTRA_MODE = "mode"

        const val MODE_PUSH_TO_TALK = "push_to_talk"
        const val MODE_HANDS_FREE = "hands_free"
    }

    // ── Voice State ────────────────────────────────────────

    enum class VoiceState {
        IDLE, LISTENING, PROCESSING, SPEAKING, ERROR
    }

    data class VoiceEvent(
        val state: VoiceState,
        val text: String? = null,
        val isWakeWordDetected: Boolean = false,
        val error: String? = null
    )

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _voiceEvents = MutableSharedFlow<VoiceEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = SharedFlow.OverflowStrategy.DROP_OLDEST
    )
    val voiceEvents: SharedFlow<VoiceEvent> = _voiceEvents.asSharedFlow()

    private val _conversationHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val conversationHistory: StateFlow<List<Pair<String, String>>> = _conversationHistory.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var currentMode: String = MODE_PUSH_TO_TALK
    private var isHandsFreeListening = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ── Lifecycle ──────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        initSpeechRecognizer()
        initTTS()
        Log.i(TAG, "VoiceService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_LISTENING -> startListening()
            ACTION_STOP_LISTENING -> stopListening()
            ACTION_SET_MODE -> {
                currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_PUSH_TO_TALK
                if (currentMode == MODE_HANDS_FREE) {
                    startListening()
                } else {
                    stopListening()
                }
            }
            ACTION_SPEAK -> {
                val text = intent.getStringExtra(EXTRA_TEXT)
                if (text != null) speak(text)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopListening()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Speech Recognition ─────────────────────────────────

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _voiceState.value = VoiceState.LISTENING
                    serviceScope.launch {
                        _voiceEvents.emit(VoiceEvent(state = VoiceState.LISTENING))
                    }
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _voiceState.value = VoiceState.PROCESSING
                }

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech error: $error")
                    // Error 7 = speech timeout (normal for PTT release)
                    // Error 6 = no match
                    if (error != 7 && error != 6) {
                        _voiceState.value = VoiceState.ERROR
                        serviceScope.launch {
                            _voiceEvents.emit(VoiceEvent(
                                state = VoiceState.ERROR,
                                error = "Recognition error: $error"
                            ))
                        }
                    } else {
                        _voiceState.value = VoiceState.IDLE
                    }
                    if (currentMode == MODE_HANDS_FREE && error != 8) {
                        // Restart listening in hands-free mode (error 8 = client cancelled)
                        serviceScope.launch {
                            delay(500)
                            if (isHandsFreeListening) startListening()
                        }
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestMatch = matches?.firstOrNull()
                    if (bestMatch != null) {
                        processTranscription(bestMatch)
                    } else {
                        _voiceState.value = VoiceState.IDLE
                    }
                    // Restart listening in hands-free mode
                    if (currentMode == MODE_HANDS_FREE) {
                        serviceScope.launch {
                            delay(300)
                            if (isHandsFreeListening) startListening()
                        }
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Optional: emit partial results for UI feedback
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startListening() {
        _voiceState.value = VoiceState.LISTENING
        isHandsFreeListening = true

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening: ${e.message}")
            _voiceState.value = VoiceState.ERROR
            serviceScope.launch {
                _voiceEvents.emit(VoiceEvent(
                    state = VoiceState.ERROR,
                    error = e.message
                ))
            }
        }
    }

    private fun stopListening() {
        isHandsFreeListening = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping listener: ${e.message}")
        }
        _voiceState.value = VoiceState.IDLE
    }

    private fun processTranscription(text: String) {
        Log.i(TAG, "Transcribed: $text")

        // Check for wake word
        val lowerText = text.lowercase().trim()
        val wakeWordDetected = lowerText.contains(WAKE_WORD) || lowerText.startsWith(WAKE_WORD_SHORT)

        serviceScope.launch {
            _voiceEvents.emit(VoiceEvent(
                state = VoiceState.PROCESSING,
                text = text,
                isWakeWordDetected = wakeWordDetected
            ))

            // Strip wake word from command
            var commandText = text
            if (wakeWordDetected) {
                commandText = text.replace(Regex("(?i)\\b(hey\\s+)?hermes\\b\\s*"), "").trim()
            }

            if (commandText.isNotEmpty()) {
                // Add to conversation history
                _conversationHistory.value = _conversationHistory.value + ("user" to commandText)

                // Forward to Hermes Brain
                val intent = Intent("com.hermes.companion.action.VOICE_COMMAND").apply {
                    putExtra("command", commandText)
                    putExtra("wake_word", wakeWordDetected)
                    putExtra("confidence", 1f)
                }
                sendBroadcast(intent)
            }
        }
    }

    // ── Text-to-Speech ─────────────────────────────────────

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                        serviceScope.launch {
                            _voiceEvents.emit(VoiceEvent(state = VoiceState.IDLE))
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _voiceState.value = VoiceState.ERROR
                    }
                })
                ttsReady = true
                Log.i(TAG, "TTS initialized")
            } else {
                Log.e(TAG, "TTS init failed: $status")
            }
        }
    }

    fun speak(text: String) {
        if (!ttsReady) {
            Log.w(TAG, "TTS not ready, queuing: $text")
            return
        }
        _voiceState.value = VoiceState.SPEAKING
        _conversationHistory.value = _conversationHistory.value + ("assistant" to text)

        val utteranceId = "utterance_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    // ── Push-to-Talk Helpers ───────────────────────────────

    /** Called by UI when push-to-talk button is pressed. */
    fun onPushToTalkStart() {
        if (currentMode == MODE_PUSH_TO_TALK) {
            startListening()
        }
    }

    /** Called by UI when push-to-talk button is released. */
    fun onPushToTalkEnd() {
        if (currentMode == MODE_PUSH_TO_TALK) {
            stopListening()
        }
    }
}
