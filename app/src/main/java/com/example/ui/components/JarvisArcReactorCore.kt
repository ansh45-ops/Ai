package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisTeal
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorCore(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isActive: Boolean = true,
    isSpeaking: Boolean = false,
    energyLevel: Float = 1.0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core_anim")

    // Slow clockwise rotation
    val rotationClockwise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_cw"
    )

    // Faster counter-clockwise rotation
    val rotationCounter by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 3000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_ccw"
    )

    // Core pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 600 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) * 0.92f

            // 1. Outer ambient radial glow
            val glowColor = if (isSpeaking) JarvisCyanLight.copy(alpha = 0.35f) else JarvisCyan.copy(alpha = 0.2f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = center,
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f,
                center = center
            )

            // 2. Outermost static circular ring
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Rotating outer tick segments (Clockwise)
            rotate(degrees = rotationClockwise, pivot = center) {
                val tickCount = 36
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI / 180f)
                    val isMajor = i % 3 == 0
                    val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                    val tickColor = if (isMajor) JarvisCyanLight else JarvisCyan.copy(alpha = 0.4f)
                    val startR = radius - tickLen
                    val start = Offset(
                        center.x + (startR * cos(angle)).toFloat(),
                        center.y + (startR * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        center.x + (radius * cos(angle)).toFloat(),
                        center.y + (radius * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = tickColor,
                        start = start,
                        end = end,
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Arc segments
                drawArc(
                    color = JarvisCyan,
                    startAngle = 10f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = JarvisCyan,
                    startAngle = 130f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = JarvisCyan,
                    startAngle = 250f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Middle Ring (Counter-Clockwise)
            val midRadius = radius * 0.72f
            rotate(degrees = rotationCounter, pivot = center) {
                drawCircle(
                    color = JarvisTeal.copy(alpha = 0.3f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3 segmented energy arcs
                for (a in listOf(0f, 120f, 240f)) {
                    drawArc(
                        color = JarvisTeal,
                        startAngle = a,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner spokes
                val spokeCount = 6
                for (i in 0 until spokeCount) {
                    val angle = (i * 360f / spokeCount) * (Math.PI / 180f)
                    val r1 = midRadius * 0.45f
                    val r2 = midRadius * 0.85f
                    drawLine(
                        color = JarvisCyan.copy(alpha = 0.6f),
                        start = Offset(
                            center.x + (r1 * cos(angle)).toFloat(),
                            center.y + (r1 * sin(angle)).toFloat()
                        ),
                        end = Offset(
                            center.x + (r2 * cos(angle)).toFloat(),
                            center.y + (r2 * sin(angle)).toFloat()
                        ),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // 5. Pulsing Inner Core
            val coreRadius = (radius * 0.36f) * (if (isActive) pulseScale else 0.8f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanLight,
                        JarvisCyan,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.2f
                ),
                radius = coreRadius,
                center = center
            )

            // Center triangular arc reactor prism
            rotate(degrees = rotationClockwise * 1.5f, pivot = center) {
                val triRadius = coreRadius * 0.55f
                val p1 = Offset(center.x, center.y - triRadius)
                val p2 = Offset(
                    center.x + (triRadius * cos(30 * Math.PI / 180)).toFloat(),
                    center.y + (triRadius * sin(30 * Math.PI / 180)).toFloat()
                )
                val p3 = Offset(
                    center.x - (triRadius * cos(30 * Math.PI / 180)).toFloat(),
                    center.y + (triRadius * sin(30 * Math.PI / 180)).toFloat()
                )
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    close()
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
