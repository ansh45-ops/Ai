package com.example.data.model

data class ScreenTargetRegion(
    val left: Float = 0.15f,
    val top: Float = 0.25f,
    val right: Float = 0.85f,
    val bottom: Float = 0.65f
) {
    val width: Float get() = (right - left).coerceAtLeast(0.05f)
    val height: Float get() = (bottom - top).coerceAtLeast(0.05f)

    fun clamped(): ScreenTargetRegion {
        val l = left.coerceIn(0f, 0.9f)
        val t = top.coerceIn(0f, 0.9f)
        val r = right.coerceIn(l + 0.05f, 1f)
        val b = bottom.coerceIn(t + 0.05f, 1f)
        return ScreenTargetRegion(l, t, r, b)
    }
}
