package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisMode
import com.example.service.JarvisFloatingService
import com.example.ui.MainViewModel
import com.example.ui.components.JarvisArcReactorCore
import com.example.ui.components.JarvisHoloCard
import com.example.ui.theme.JarvisAlert
import com.example.ui.theme.JarvisAmber
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
import kotlin.math.roundToInt

@Composable
fun FloatingOverlayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()

    // Periodically re-check permission
    LaunchedEffect(Unit) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Header Card
        JarvisHoloCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = JarvisCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "FLOATING ASSISTANT HUD",
                        color = JarvisCyan,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "SYSTEM_ALERT_WINDOW • Persistent Sci-Fi Overlay",
                        color = JarvisTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Permission & Service Controls Card
        JarvisHoloCard(borderColor = if (hasOverlayPermission) JarvisTeal else JarvisAmber) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasOverlayPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (hasOverlayPermission) JarvisTeal else JarvisAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasOverlayPermission) "OVERLAY PERMISSION: GRANTED" else "OVERLAY PERMISSION: REQUIRED",
                            color = if (hasOverlayPermission) JarvisTeal else JarvisAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!hasOverlayPermission) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + context.packageName)
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open settings: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_overlay_permission_button")
                        ) {
                            Text(
                                text = "GRANT",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "Android requires 'Display over other apps' to float JARVIS above home screens, browsers, games, and third-party apps.",
                    color = JarvisTextMuted,
                    fontSize = 11.sp
                )

                // Foreground Service Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SYSTEM-WIDE OVERLAY SERVICE",
                            color = JarvisTextPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (overlayEnabled) "Active in foreground" else "Service inactive",
                            color = if (overlayEnabled) JarvisCyan else JarvisTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Switch(
                        checked = overlayEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (Settings.canDrawOverlays(context)) {
                                    viewModel.setOverlayEnabled(true)
                                    val intent = Intent(context, JarvisFloatingService::class.java)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                    Toast.makeText(context, "JARVIS Floating HUD Activated", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please grant Overlay Permission first", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.setOverlayEnabled(false)
                                val intent = Intent(context, JarvisFloatingService::class.java).apply {
                                    action = JarvisFloatingService.ACTION_STOP
                                }
                                context.stopService(intent)
                                Toast.makeText(context, "JARVIS Floating HUD Dismissed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisCyan,
                            checkedTrackColor = JarvisSurface,
                            uncheckedThumbColor = JarvisTextMuted,
                            uncheckedTrackColor = JarvisBackground
                        ),
                        modifier = Modifier.testTag("floating_service_switch")
                    )
                }
            }
        }

        // In-App Interactive Sandbox / Simulator Header
        Text(
            text = "INTERACTIVE FLOATING HUD SIMULATOR",
            color = JarvisCyanLight,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Test the floating arc-reactor button directly in this viewport. Drag the circular JARVIS core anywhere, or tap to deploy the movable holographic panel.",
            color = JarvisTextSecondary,
            fontSize = 11.sp
        )

        // Sandbox Area with Draggable Floating Arc Reactor and Expandable Panel
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF0F1E36), Color(0xFF060B14))
                    )
                )
                .border(1.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            val maxW = constraints.maxWidth.toFloat()
            val maxH = constraints.maxHeight.toFloat()

            var offsetX by remember { mutableFloatStateOf(maxW * 0.15f) }
            var offsetY by remember { mutableFloatStateOf(maxH * 0.15f) }
            var isExpanded by remember { mutableStateOf(false) }

            // Simulated App Background
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "[SIMULATED EXTERNAL WORKSPACE]",
                    color = JarvisTextMuted.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(14.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
            }

            // Draggable Floating Arc Reactor Widget
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(0f, maxW - 240f)
                            offsetY = (offsetY + dragAmount.y).coerceIn(0f, maxH - 260f)
                        }
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Floating Circle Logo Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF00E5FF), Color(0xFF09162B))
                                )
                            )
                            .border(2.dp, JarvisCyanLight, CircleShape)
                            .clickable {
                                isExpanded = !isExpanded
                                viewModel.triggerHaptic()
                            }
                            .testTag("simulated_floating_arc_reactor"),
                        contentAlignment = Alignment.Center
                    ) {
                        JarvisArcReactorCore(
                            size = 52.dp,
                            isActive = true
                        )
                    }

                    // Expanded Holographic Mini-HUD
                    AnimatedVisibility(visible = isExpanded) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .width(220.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(JarvisSurface.copy(alpha = 0.95f))
                                .border(1.dp, JarvisCyan, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "JARVIS MINI-HUD",
                                        color = JarvisCyan,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { isExpanded = false },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = JarvisTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.setMode(JarvisMode.VOICE) },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Mic, null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Voice Directive", color = JarvisTextPrimary, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.setMode(JarvisMode.SCREEN_ANALYSIS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ScreenSearchDesktop, null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Screen Analysis", color = JarvisTextPrimary, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.setMode(JarvisMode.CHAT) },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInNew, null, tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Expand Full AI", color = JarvisCyanLight, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Specs & Capabilities Card
        JarvisHoloCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "OVERLAY ARCHITECTURE SPECIFICATIONS:",
                    color = JarvisCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Uses WindowManager.addView() with TYPE_APPLICATION_OVERLAY.\n• MotionEvent.ACTION_DOWN/MOVE/UP handles frictionless pixel drag.\n• Hosted inside an Android Foreground Service with continuous lifecycle.\n• Quick-actions invoke deep-link intents back into JarvisActivity.",
                    color = JarvisTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
