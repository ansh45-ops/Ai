package com.example.data.model

enum class JarvisMode(val label: String, val code: String) {
    CHAT("CHAT", "M-01"),
    VOICE("VOICE", "M-02"),
    FLOATING("FLOAT HUD", "M-03"),
    SCREEN_ANALYSIS("VISION HUD", "M-04"),
    SETTINGS("SETTINGS", "M-05"),
    ARCHITECTURE("SYSTEM ARCH", "M-06")
}
