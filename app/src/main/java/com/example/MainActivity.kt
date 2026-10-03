package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.model.JarvisMode
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisTopBar
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FloatingOverlayScreen
import com.example.ui.screens.ScreenAnalysisScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleModeIntent(intent)

        setContent {
            MyApplicationTheme {
                JarvisMainContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleModeIntent(intent)
    }

    private fun handleModeIntent(intent: Intent?) {
        val modeStr = intent?.getStringExtra("EXTRA_MODE")
        if (!modeStr.isNullOrEmpty()) {
            try {
                val targetMode = JarvisMode.valueOf(modeStr)
                viewModel.setMode(targetMode)
            } catch (e: Exception) {
                // Ignore invalid mode
            }
        }
    }
}

@Composable
fun JarvisMainContent(viewModel: MainViewModel) {
    val currentMode by viewModel.currentMode.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()

    // Android back navigation: if on a sub-screen, return to CHAT mode
    if (currentMode != JarvisMode.CHAT) {
        BackHandler {
            viewModel.setMode(JarvisMode.CHAT)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        topBar = {
            JarvisTopBar(
                currentMode = currentMode,
                onModeSelected = { viewModel.setMode(it) },
                voiceEnabled = isVoiceEnabled,
                onToggleVoice = { viewModel.toggleVoiceEnabled() },
                isSpeaking = isSpeaking
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            Crossfade(
                targetState = currentMode,
                label = "screen_transition"
            ) { mode ->
                when (mode) {
                    JarvisMode.CHAT -> ChatScreen(viewModel = viewModel)
                    JarvisMode.VOICE -> VoiceScreen(viewModel = viewModel)
                    JarvisMode.FLOATING -> FloatingOverlayScreen(viewModel = viewModel)
                    JarvisMode.SCREEN_ANALYSIS -> ScreenAnalysisScreen(viewModel = viewModel)
                    JarvisMode.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    JarvisMode.ARCHITECTURE -> ArchitectureScreen()
                }
            }
        }
    }
}
