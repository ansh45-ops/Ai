package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_ai_prefs", Context.MODE_PRIVATE)

    var voiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) = prefs.edit().putBoolean("voice_enabled", value).apply()

    var autoSpeakResponses: Boolean
        get() = prefs.getBoolean("auto_speak_responses", true)
        set(value) = prefs.edit().putBoolean("auto_speak_responses", value).apply()

    var voiceSpeed: Float
        get() = prefs.getFloat("voice_speed", 1.0f)
        set(value) = prefs.edit().putFloat("voice_speed", value).apply()

    var voicePitch: Float
        get() = prefs.getFloat("voice_pitch", 1.05f)
        set(value) = prefs.edit().putFloat("voice_pitch", value).apply()

    var selectedModel: String
        get() = prefs.getString("selected_model", "gemini-3.5-flash") ?: "gemini-3.5-flash"
        set(value) = prefs.edit().putString("selected_model", value).apply()

    var apiKeyOverride: String
        get() = prefs.getString("api_key_override", "") ?: ""
        set(value) = prefs.edit().putString("api_key_override", value).apply()

    var hapticsEnabled: Boolean
        get() = prefs.getBoolean("haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("haptics_enabled", value).apply()

    var overlayEnabled: Boolean
        get() = prefs.getBoolean("overlay_enabled", false)
        set(value) = prefs.edit().putBoolean("overlay_enabled", value).apply()

    var languageCode: String
        get() = prefs.getString("language_code", "en-US") ?: "en-US"
        set(value) = prefs.edit().putString("language_code", value).apply()
}
