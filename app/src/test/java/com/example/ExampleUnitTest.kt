package com.example

import com.example.data.model.ScreenTargetRegion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun targetRegion_clampingValid() {
        val region = ScreenTargetRegion(left = -0.2f, top = 0.1f, right = 1.5f, bottom = 0.9f)
        val clamped = region.clamped()
        assertTrue(clamped.left >= 0f)
        assertTrue(clamped.right <= 1f)
        assertTrue(clamped.width >= 0.05f)
        assertTrue(clamped.height >= 0.05f)
    }
}
