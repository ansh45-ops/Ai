package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScreenTargetRegion
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisTeal
import kotlin.math.roundToInt

@Composable
fun JarvisScreenSelector(
    region: ScreenTargetRegion,
    onRegionChange: (ScreenTargetRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = constraints.maxWidth.toFloat().coerceAtLeast(100f)
        val totalHeight = constraints.maxHeight.toFloat().coerceAtLeast(100f)

        val handleSizeDp = 28.dp
        val handleRadiusPx = (handleSizeDp.value * 2f).coerceAtLeast(24f)

        val leftPx = region.left * totalWidth
        val topPx = region.top * totalHeight
        val rightPx = region.right * totalWidth
        val bottomPx = region.bottom * totalHeight
        val widthPx = rightPx - leftPx
        val heightPx = bottomPx - topPx

        // Canvas for darkened mask, grid, border, and crosshairs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Darkened outer mask outside crop rect
            // Top rect
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                topLeft = Offset(0f, 0f),
                size = Size(canvasW, topPx)
            )
            // Bottom rect
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                topLeft = Offset(0f, bottomPx),
                size = Size(canvasW, (canvasH - bottomPx).coerceAtLeast(0f))
            )
            // Left rect
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                topLeft = Offset(0f, topPx),
                size = Size(leftPx, heightPx)
            )
            // Right rect
            drawRect(
                color = Color.Black.copy(alpha = 0.65f),
                topLeft = Offset(rightPx, topPx),
                size = Size((canvasW - rightPx).coerceAtLeast(0f), heightPx)
            )

            // 2. Neon cyan crop border
            drawRect(
                color = JarvisCyan,
                topLeft = Offset(leftPx, topPx),
                size = Size(widthPx, heightPx),
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Rule-of-thirds sci-fi dashed grid lines
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val strokeGrid = Stroke(width = 1.dp.toPx(), pathEffect = dashedEffect)

            // Vertical grid lines
            val oneThirdX = leftPx + (widthPx / 3f)
            val twoThirdX = leftPx + (2 * widthPx / 3f)
            drawLine(JarvisCyan.copy(alpha = 0.35f), Offset(oneThirdX, topPx), Offset(oneThirdX, bottomPx), strokeGrid.width, pathEffect = dashedEffect)
            drawLine(JarvisCyan.copy(alpha = 0.35f), Offset(twoThirdX, topPx), Offset(twoThirdX, bottomPx), strokeGrid.width, pathEffect = dashedEffect)

            // Horizontal grid lines
            val oneThirdY = topPx + (heightPx / 3f)
            val twoThirdY = topPx + (2 * heightPx / 3f)
            drawLine(JarvisCyan.copy(alpha = 0.35f), Offset(leftPx, oneThirdY), Offset(rightPx, oneThirdY), strokeGrid.width, pathEffect = dashedEffect)
            drawLine(JarvisCyan.copy(alpha = 0.35f), Offset(leftPx, twoThirdY), Offset(rightPx, twoThirdY), strokeGrid.width, pathEffect = dashedEffect)

            // 4. Center Targeting Reticle / Crosshair
            val centerX = leftPx + (widthPx / 2f)
            val centerY = topPx + (heightPx / 2f)
            val crosshairSize = 18.dp.toPx()

            // Crosshair lines
            drawLine(JarvisCyanLight, Offset(centerX - crosshairSize, centerY), Offset(centerX + crosshairSize, centerY), strokeWidth = 1.5.dp.toPx())
            drawLine(JarvisCyanLight, Offset(centerX, centerY - crosshairSize), Offset(centerX, centerY + crosshairSize), strokeWidth = 1.5.dp.toPx())
            // Targeting circle
            drawCircle(
                color = JarvisCyanLight,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )

            // Corner brackets
            val bracketLen = 16.dp.toPx()
            val bracketStroke = 3.dp.toPx()
            // TL
            drawLine(JarvisCyanLight, Offset(leftPx, topPx), Offset(leftPx + bracketLen, topPx), bracketStroke)
            drawLine(JarvisCyanLight, Offset(leftPx, topPx), Offset(leftPx, topPx + bracketLen), bracketStroke)
            // TR
            drawLine(JarvisCyanLight, Offset(rightPx - bracketLen, topPx), Offset(rightPx, topPx), bracketStroke)
            drawLine(JarvisCyanLight, Offset(rightPx, topPx), Offset(rightPx, topPx + bracketLen), bracketStroke)
            // BL
            drawLine(JarvisCyanLight, Offset(leftPx, bottomPx), Offset(leftPx + bracketLen, bottomPx), bracketStroke)
            drawLine(JarvisCyanLight, Offset(leftPx, bottomPx - bracketLen), Offset(leftPx, bottomPx), bracketStroke)
            // BR
            drawLine(JarvisCyanLight, Offset(rightPx - bracketLen, bottomPx), Offset(rightPx, bottomPx), bracketStroke)
            drawLine(JarvisCyanLight, Offset(rightPx, bottomPx - bracketLen), Offset(rightPx, bottomPx), bracketStroke)
        }

        // Draggable Center Pan Area
        Box(
            modifier = Modifier
                .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                .size((widthPx / totalWidth * constraints.maxWidth).dp, (heightPx / totalHeight * constraints.maxHeight).dp)
                .pointerInput(totalWidth, totalHeight, region) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val dx = dragAmount.x / totalWidth
                        val dy = dragAmount.y / totalHeight
                        val w = region.right - region.left
                        val h = region.bottom - region.top

                        val newLeft = (region.left + dx).coerceIn(0f, 1f - w)
                        val newTop = (region.top + dy).coerceIn(0f, 1f - h)
                        onRegionChange(
                            ScreenTargetRegion(
                                left = newLeft,
                                top = newTop,
                                right = newLeft + w,
                                bottom = newTop + h
                            )
                        )
                    }
                }
        )

        // 4 Corner Draggable Touch Handles
        // 1. Top-Left Handle
        HandleCircle(
            x = leftPx,
            y = topPx,
            onDrag = { dx, dy ->
                val newL = (region.left + dx / totalWidth).coerceIn(0f, region.right - 0.08f)
                val newT = (region.top + dy / totalHeight).coerceIn(0f, region.bottom - 0.08f)
                onRegionChange(region.copy(left = newL, top = newT))
            }
        )

        // 2. Top-Right Handle
        HandleCircle(
            x = rightPx,
            y = topPx,
            onDrag = { dx, dy ->
                val newR = (region.right + dx / totalWidth).coerceIn(region.left + 0.08f, 1f)
                val newT = (region.top + dy / totalHeight).coerceIn(0f, region.bottom - 0.08f)
                onRegionChange(region.copy(right = newR, top = newT))
            }
        )

        // 3. Bottom-Left Handle
        HandleCircle(
            x = leftPx,
            y = bottomPx,
            onDrag = { dx, dy ->
                val newL = (region.left + dx / totalWidth).coerceIn(0f, region.right - 0.08f)
                val newB = (region.bottom + dy / totalHeight).coerceIn(region.top + 0.08f, 1f)
                onRegionChange(region.copy(left = newL, bottom = newB))
            }
        )

        // 4. Bottom-Right Handle
        HandleCircle(
            x = rightPx,
            y = bottomPx,
            onDrag = { dx, dy ->
                val newR = (region.right + dx / totalWidth).coerceIn(region.left + 0.08f, 1f)
                val newB = (region.bottom + dy / totalHeight).coerceIn(region.top + 0.08f, 1f)
                onRegionChange(region.copy(right = newR, bottom = newB))
            }
        )

        // Telemetry HUD coordinates readout at bottom-left of target
        val displayW = (widthPx).roundToInt()
        val displayH = (heightPx).roundToInt()
        val displayX = (leftPx).roundToInt()
        val displayY = (topPx).roundToInt()

        Text(
            text = "TARGET: [X:$displayX, Y:$displayY | W:$displayW, H:$displayH]",
            color = JarvisCyanLight,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .offset {
                    val labelY = if (topPx > 40) (topPx - 26).roundToInt() else (bottomPx + 6).roundToInt()
                    IntOffset(leftPx.roundToInt().coerceAtLeast(10), labelY)
                }
                .background(Color.Black.copy(alpha = 0.75f))
        )
    }
}

@Composable
private fun HandleCircle(
    x: Float,
    y: Float,
    onDrag: (Float, Float) -> Unit
) {
    val handleSize = 32.dp
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (x - handleSize.toPx() / 2f).roundToInt(),
                    (y - handleSize.toPx() / 2f).roundToInt()
                )
            }
            .size(handleSize)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(JarvisCyanLight, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(JarvisTeal, CircleShape)
        )
    }
}
