package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.JarvisHoloCard
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTeal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ArchitectureScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        JarvisHoloCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeveloperBoard,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM ARCHITECTURE BLUEPRINT",
                        color = JarvisCyan,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Native Android Implementation & Web Boundaries",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Section 1: Web Sandbox vs Native Android Explanation
        JarvisHoloCard(borderColor = JarvisAmber) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = JarvisAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WEB BROWSER BOUNDARIES VS NATIVE APIS",
                        color = JarvisAmber,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "A browser PWA runs inside a strict sandboxed web environment and CANNOT draw arbitrary floating windows over other native Android apps, nor inspect other applications' screens without explicit user casting permission.\n\n" +
                            "This application delivers the authentic Native Android Kotlin implementation using the exact system APIs required:",
                    color = JarvisTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Section 2: Implemented Native Android Components
        JarvisHoloCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "NATIVE ANDROID SUBSYSTEMS BUILT:",
                    color = JarvisCyanLight,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                ArchitectureItem(
                    icon = Icons.Default.Smartphone,
                    title = "1. SYSTEM_ALERT_WINDOW (Floating Assistant)",
                    details = "Implemented in 'JarvisFloatingService.kt'. Binds to WindowManager with TYPE_APPLICATION_OVERLAY, tracking MotionEvent touches to let the user freely drag the glowing JARVIS arc reactor and expand the mini-HUD above any running app."
                )

                ArchitectureItem(
                    icon = Icons.Default.AccountTree,
                    title = "2. MediaProjection & Crop Selection (Screen Mode)",
                    details = "Uses MediaProjectionManager with explicit system consent dialogue. Coupled with 'JarvisScreenSelector.kt' featuring 4 draggable corner handles, crosshairs, and normalized coordinate cropping before Gemini transmission."
                )

                ArchitectureItem(
                    icon = Icons.Default.Code,
                    title = "3. SpeechRecognizer & TextToSpeech (Voice Engine)",
                    details = "Implemented in 'JarvisVoiceManager.kt'. Employs Android's native SpeechRecognizer with live RMS dB volume metering feeding into 'JarvisAudioWaveform.kt', and natural speech synthesis via TextToSpeech."
                )

                ArchitectureItem(
                    icon = Icons.Default.DeveloperBoard,
                    title = "4. Gemini Multimodal REST Uplink (AI Intelligence)",
                    details = "Implemented in 'GeminiService.kt' with OkHttp 60-second timeouts. Injects API keys securely via BuildConfig (Secrets Gradle Plugin) and provides system instructions tailored for the JARVIS persona."
                )
            }
        }

        // Section 3: Package Hierarchy
        JarvisHoloCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPLICATION DIRECTORY STRUCTURE:",
                    color = JarvisCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                val fileTree = """
                    com.example/
                    ├── MainActivity.kt            (Navigation & Lifecycle)
                    ├── data/
                    │   ├── api/GeminiService.kt   (REST Multimodal Client)
                    │   ├── model/ChatMessage.kt   (Conversation Entities)
                    │   ├── model/JarvisMode.kt    (Mode Enum)
                    │   └── preferences/           (SharedPreferences Engine)
                    ├── voice/
                    │   └── JarvisVoiceManager.kt  (TTS + SpeechRecognizer)
                    ├── service/
                    │   └── JarvisFloatingService  (Foreground Overlay)
                    └── ui/
                        ├── MainViewModel.kt       (State Flow Orchestrator)
                        ├── components/
                        │   ├── JarvisArcReactorCore.kt
                        │   ├── JarvisAudioWaveform.kt
                        │   └── JarvisScreenSelector.kt
                        └── screens/
                            ├── ChatScreen.kt
                            ├── VoiceScreen.kt
                            ├── FloatingOverlayScreen.kt
                            ├── ScreenAnalysisScreen.kt
                            └── SettingsScreen.kt
                """.trimIndent()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = fileTree,
                        color = JarvisCyanLight,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchitectureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    details: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = JarvisTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = JarvisTeal,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = details,
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}
