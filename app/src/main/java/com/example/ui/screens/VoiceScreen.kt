package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MessageSender
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisArcReactorCore
import com.example.ui.components.JarvisAudioWaveform
import com.example.ui.components.JarvisHoloCard
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBorder
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.JarvisTeal
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val liveRmsDb by viewModel.voiceManager.liveRmsDb.collectAsState()
    val liveTranscript by viewModel.voiceManager.liveTranscript.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val lastJarvisMessage = messages.lastOrNull { it.sender == MessageSender.JARVIS }?.text
        ?: "Awaiting vocal command, Sir. Core audio channels open."

    val statusText = when {
        isListening -> "MICROPHONE ENGAGED // LISTENING..."
        isGenerating -> "NEURAL CORE PROCESSING INQUIRY..."
        isSpeaking -> "SYNTHESIZING VOCAL TRANSMISSION..."
        else -> "VOCAL MATRIX STANDBY // READY"
    }

    val statusColor = when {
        isListening -> JarvisTeal
        isGenerating -> JarvisCyanLight
        isSpeaking -> JarvisCyan
        else -> JarvisTextSecondary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status & Voice Toggle Header
        JarvisHoloCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "VOICE ENGINE: ANDROID TTS + SPEECH_RECOGNIZER",
                        color = JarvisTextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isVoiceEnabled) "VOICE ON" else "MUTED",
                        color = if (isVoiceEnabled) JarvisCyan else JarvisTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Switch(
                        checked = isVoiceEnabled,
                        onCheckedChange = { viewModel.toggleVoiceEnabled() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisSurfaceVariant,
                            uncheckedThumbColor = JarvisTextMuted,
                            uncheckedTrackColor = JarvisBackground
                        ),
                        modifier = Modifier.testTag("voice_talkback_toggle")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Center Pulsing Arc Reactor Core
        JarvisArcReactorCore(
            size = 230.dp,
            isActive = isListening || isSpeaking || isGenerating,
            isSpeaking = isSpeaking,
            energyLevel = if (isListening) 1.2f else 1.0f
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Animated Audio Waveform
        JarvisAudioWaveform(
            height = 54.dp,
            isActive = isListening || isSpeaking,
            rmsDb = if (isListening) liveRmsDb else if (isSpeaking) 6.5f else 0f
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Voice Transcript or Active Speech Status
        if (isListening || liveTranscript.isNotEmpty()) {
            JarvisHoloCard(
                borderColor = JarvisTeal,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Column {
                    Text(
                        text = "AUDIO RECOGNITION BUFFER:",
                        color = JarvisTeal,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (liveTranscript.isNotEmpty()) "\"$liveTranscript\"" else "Speak now, Sir...",
                        color = JarvisTextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // JARVIS Vocal Answer Card
        JarvisHoloCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "J.A.R.V.I.S. VOCAL RESPONSE:",
                        color = JarvisCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speech",
                                tint = JarvisAlert,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.speakText(lastJarvisMessage) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Replay Speech",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lastJarvisMessage,
                    color = JarvisTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Push-To-Talk Button with Sci-Fi Pulsing Ring
        val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isListening) 1.25f else 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = if (isListening) 600 else 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mic_scale"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            // Pulse outer glow circle
            Box(
                modifier = Modifier
                    .size(80.dp * pulseScale)
                    .clip(CircleShape)
                    .background(
                        if (isListening) JarvisTeal.copy(alpha = 0.25f) else JarvisCyan.copy(alpha = 0.15f)
                    )
            )

            // Inner button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) JarvisTeal else JarvisCyan
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable {
                        if (isListening) {
                            viewModel.voiceManager.stopListening()
                        } else {
                            viewModel.voiceManager.startListening(
                                onResult = { recognized ->
                                    viewModel.sendMessage(recognized)
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    .testTag("voice_talk_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Text(
            text = if (isListening) "TAP TO COMPLETE DIRECTIVE" else "TAP TO INITIATE VOCAL COMMAND",
            color = JarvisTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick vocal command prompts
        val quickVoicePrompts = listOf(
            "Report system status",
            "What are your core capabilities?",
            "Run security diagnostic",
            "Calculate quantum probability"
        )

        Text(
            text = "PRE-CONFIGURED VOCAL DIRECTIVES:",
            color = JarvisTextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickVoicePrompts.forEach { directive ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, JarvisBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.sendMessage(directive)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "▶ \"$directive\"",
                        color = JarvisCyanLight,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
