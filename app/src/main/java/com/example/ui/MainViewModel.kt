package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.model.ChatMessage
import com.example.data.model.JarvisMode
import com.example.data.model.MessageSender
import com.example.data.model.ScreenTargetRegion
import com.example.data.preferences.JarvisPreferences
import com.example.voice.JarvisVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prefs = JarvisPreferences(application)
    val voiceManager = JarvisVoiceManager(application)
    private val geminiService = GeminiService()

    private val vibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    // Navigation Mode
    private val _currentMode = MutableStateFlow(JarvisMode.CHAT)
    val currentMode: StateFlow<JarvisMode> = _currentMode.asStateFlow()

    // Chat State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Screen Analysis State
    private val _targetRegion = MutableStateFlow(ScreenTargetRegion())
    val targetRegion: StateFlow<ScreenTargetRegion> = _targetRegion.asStateFlow()

    private val _currentScreenBitmap = MutableStateFlow<Bitmap?>(null)
    val currentScreenBitmap: StateFlow<Bitmap?> = _currentScreenBitmap.asStateFlow()

    private val _screenQuestion = MutableStateFlow("Analyze this selected screen sector and provide a tactical breakdown.")
    val screenQuestion: StateFlow<String> = _screenQuestion.asStateFlow()

    private val _screenAnalysisResult = MutableStateFlow<String?>(null)
    val screenAnalysisResult: StateFlow<String?> = _screenAnalysisResult.asStateFlow()

    private val _isAnalyzingScreen = MutableStateFlow(false)
    val isAnalyzingScreen: StateFlow<Boolean> = _isAnalyzingScreen.asStateFlow()

    // Preferences mirrors
    val isVoiceEnabled = MutableStateFlow(prefs.voiceEnabled)
    val autoSpeak = MutableStateFlow(prefs.autoSpeakResponses)
    val voiceSpeed = MutableStateFlow(prefs.voiceSpeed)
    val voicePitch = MutableStateFlow(prefs.voicePitch)
    val selectedModel = MutableStateFlow(prefs.selectedModel)
    val apiKeyOverride = MutableStateFlow(prefs.apiKeyOverride)
    val overlayEnabled = MutableStateFlow(prefs.overlayEnabled)

    init {
        // Welcome message from JARVIS
        val initialGreeting = ChatMessage(
            sender = MessageSender.JARVIS,
            text = "Good day, Sir. J.A.R.V.I.S. neural systems are fully operational. I am prepared to assist with tactical data analysis, conversational inquiries, vocal directives, or screen inspection."
        )
        _messages.value = listOf(initialGreeting)

        // Generate initial sample high-tech screenshot for vision analysis
        loadSampleScreenshot("CODE")
    }

    fun setMode(mode: JarvisMode) {
        _currentMode.value = mode
        triggerHaptic()
    }

    fun updateScreenRegion(region: ScreenTargetRegion) {
        _targetRegion.value = region.clamped()
    }

    fun setScreenQuestion(question: String) {
        _screenQuestion.value = question
    }

    fun setScreenBitmap(bitmap: Bitmap) {
        _currentScreenBitmap.value = bitmap
    }

    fun toggleVoiceEnabled() {
        val next = !isVoiceEnabled.value
        isVoiceEnabled.value = next
        prefs.voiceEnabled = next
        if (!next) {
            voiceManager.stopSpeaking()
        }
    }

    fun setVoiceSpeed(speed: Float) {
        voiceSpeed.value = speed
        prefs.voiceSpeed = speed
    }

    fun setVoicePitch(pitch: Float) {
        voicePitch.value = pitch
        prefs.voicePitch = pitch
    }

    fun setSelectedModel(model: String) {
        selectedModel.value = model
        prefs.selectedModel = model
    }

    fun setApiKeyOverride(key: String) {
        apiKeyOverride.value = key
        prefs.apiKeyOverride = key
    }

    fun setOverlayEnabled(enabled: Boolean) {
        overlayEnabled.value = enabled
        prefs.overlayEnabled = enabled
    }

    fun triggerHaptic() {
        if (prefs.hapticsEnabled) {
            try {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            } catch (e: Exception) {
                // Ignore if permission or hardware missing
            }
        }
    }

    fun sendMessage(userText: String, imageBitmap: Bitmap? = null) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() && imageBitmap == null) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = trimmed,
            imageBitmap = imageBitmap
        )
        _messages.value = _messages.value + userMessage
        triggerHaptic()

        _isGenerating.value = true

        viewModelScope.launch {
            val result = geminiService.generateResponse(
                prompt = trimmed,
                modelName = selectedModel.value,
                imageBitmap = imageBitmap,
                apiKeyOverride = apiKeyOverride.value
            )

            _isGenerating.value = false

            result.fold(
                onSuccess = { replyText ->
                    val jarvisMsg = ChatMessage(
                        sender = MessageSender.JARVIS,
                        text = replyText
                    )
                    _messages.value = _messages.value + jarvisMsg

                    // Read aloud if voice enabled
                    if (isVoiceEnabled.value && prefs.autoSpeakResponses) {
                        speakText(replyText)
                    }
                },
                onFailure = { error ->
                    val errorMsg = ChatMessage(
                        sender = MessageSender.JARVIS,
                        text = "Diagnostics Alert: ${error.localizedMessage ?: "Unknown malfunction"}",
                        isError = true
                    )
                    _messages.value = _messages.value + errorMsg
                }
            )
        }
    }

    fun speakText(text: String) {
        if (isVoiceEnabled.value) {
            voiceManager.speak(text, pitch = voicePitch.value, speed = voiceSpeed.value)
        }
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                sender = MessageSender.JARVIS,
                text = "Memory registers cleared, Sir. Ready for new instructions."
            )
        )
        triggerHaptic()
    }

    fun analyzeSelectedScreenRegion() {
        val sourceBitmap = _currentScreenBitmap.value ?: return
        val region = _targetRegion.value

        val cropX = (region.left * sourceBitmap.width).toInt().coerceIn(0, sourceBitmap.width - 1)
        val cropY = (region.top * sourceBitmap.height).toInt().coerceIn(0, sourceBitmap.height - 1)
        val cropW = (region.width * sourceBitmap.width).toInt().coerceIn(1, sourceBitmap.width - cropX)
        val cropH = (region.height * sourceBitmap.height).toInt().coerceIn(1, sourceBitmap.height - cropY)

        val cropped = try {
            Bitmap.createBitmap(sourceBitmap, cropX, cropY, cropW, cropH)
        } catch (e: Exception) {
            sourceBitmap
        }

        _isAnalyzingScreen.value = true
        _screenAnalysisResult.value = null
        triggerHaptic()

        viewModelScope.launch {
            val result = geminiService.generateResponse(
                prompt = _screenQuestion.value,
                modelName = if (selectedModel.value == "gemini-3.1-pro-preview") "gemini-3.1-pro-preview" else "gemini-3.5-flash",
                imageBitmap = cropped,
                apiKeyOverride = apiKeyOverride.value
            )

            _isAnalyzingScreen.value = false

            result.fold(
                onSuccess = { analysis ->
                    _screenAnalysisResult.value = analysis
                    if (isVoiceEnabled.value && prefs.autoSpeakResponses) {
                        speakText(analysis)
                    }
                },
                onFailure = { err ->
                    _screenAnalysisResult.value = "Vision Telemetry Error: ${err.localizedMessage}"
                }
            )
        }
    }

    fun loadSampleScreenshot(type: String) {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw futuristic simulated screens
        val bgPaint = Paint().apply { color = Color.parseColor("#060D1A") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val cyanPaint = Paint().apply {
            color = Color.parseColor("#00E5FF")
            textSize = 34f
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#E0F7FA")
            textSize = 28f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#2200E5FF")
            strokeWidth = 2f
        }

        // Draw grid
        for (x in 0..width step 80) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), linePaint)
        }
        for (y in 0..height step 80) {
            canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), linePaint)
        }

        when (type) {
            "CODE" -> {
                canvas.drawText("/// QUANTUM FLUX ENGINE - KOTLIN SOURCE", 80f, 160f, cyanPaint)
                val codeLines = listOf(
                    "fun executeQuantumLeap(coherence: Float): Result<WarpVector> {",
                    "    if (coherence < 0.85f) {",
                    "        // CRITICAL DEFECT: Null pointer in anti-matter buffer",
                    "        throw BufferOverflowException(\"Containment field unstable\")",
                    "    }",
                    "    val reactorCore = ArcReactor.initialize(pulse = 4200)",
                    "    val warpMatrix = reactorCore.calculateHyperDrive(coherence)",
                    "    return Result.success(warpMatrix)",
                    "}",
                    "",
                    "// Telemetry stream: Latency 4.2ms | Efficiency 98.7%",
                    "val diagnostics = DiagnosticsEngine.scanSector(Sector.ALPHA_7)"
                )
                var yOffset = 260f
                for (line in codeLines) {
                    canvas.drawText(line, 80f, yOffset, textPaint)
                    yOffset += 50f
                }
            }
            "CIRCUIT" -> {
                canvas.drawText("/// HOLOGRAPHIC CIRCUITRY & ENERGY MATRIX", 80f, 160f, cyanPaint)
                canvas.drawText("PRIMARY ARC CAPACITOR: 480 THz", 80f, 240f, textPaint)
                canvas.drawText("NEURAL BUS BANDWIDTH: 1.2 TB/s", 80f, 300f, textPaint)
                canvas.drawText("QUANTUM COHERENCE RATIO: 99.42%", 80f, 360f, textPaint)
                canvas.drawText("THERMAL EMISSION: 312 Kelvin [STABLE]", 80f, 420f, textPaint)
            }
            else -> {
                canvas.drawText("/// TACTICAL TELEMETRY DASHBOARD", 80f, 160f, cyanPaint)
                canvas.drawText("GLOBAL RADAR SWEEP: ALL NODES SYNCED", 80f, 240f, textPaint)
                canvas.drawText("ACTIVE THREAT LEVEL: MINIMAL (CODE GREEN)", 80f, 300f, textPaint)
                canvas.drawText("CORE ENERGY RESERVES: 94.2% ARC POWER", 80f, 360f, textPaint)
            }
        }

        _currentScreenBitmap.value = bitmap
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
