package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisTeal
import kotlin.math.sin

@Composable
fun JarvisAudioWaveform(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    isActive: Boolean = false,
    rmsDb: Float = 0f,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(3f)
        val spacing = (width - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)

        val gradient = Brush.verticalGradient(
            colors = listOf(
                JarvisCyanLight,
                JarvisCyan,
                JarvisTeal,
                JarvisBlue
            )
        )

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            // Mathematical frequency variation + animated sine phase
            val baseSine = sin(progress * Math.PI * 4 + phase).toFloat()
            val secondHarmonic = sin(progress * Math.PI * 8 - phase * 1.2f).toFloat() * 0.5f

            // Dynamic amplification from real microphone rms or active state
            val amplitude = if (isActive) {
                val micMultiplier = (rmsDb / 10f).coerceIn(0.1f, 1.0f)
                (0.25f + 0.75f * micMultiplier) * (0.4f + 0.35f * (baseSine + secondHarmonic).coerceIn(-1f, 1f))
            } else {
                0.08f + 0.05f * sin(progress * Math.PI * 2 + phase).toFloat()
            }

            val barHeight = (canvasHeight * amplitude.coerceIn(0.06f, 0.95f)).coerceAtLeast(4f)
            val x = i * (barWidth + spacing)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
