package com.example.aura.shared.core.extensions

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
// Robolectric needed for Compose Color (it uses android.graphics.Color under the hood usually, or native calls)
// But wait, Compose Color is platform independent? 
// Actually Color(0xFF...) is simple. 
// However, the function `toLong(16)` is standard Kotlin.
// Let's try without Robolectric first. Compose UI Graphics Color is a value class wrapping a ULong.
// It shouldn't strictly require Android if just manipulating the value bits, but let's see.

class ColorExtensionsTest {

    @Test
    fun `toColor should parse 6 digit hex`() {
        val hex = "#FF0000"
        val expected = Color(0xFFFF0000)
        assertEquals(expected, hex.toColor())
    }

    @Test
    fun `toColor should parse 8 digit hex`() {
        val hex = "#FFFF0000"
        val expected = Color(0xFFFF0000)
        assertEquals(expected, hex.toColor())
    }

    @Test
    fun `toColor should handle missing hash prefix`() {
        val hex = "00FF00"
        val expected = Color(0xFF00FF00)
        assertEquals(expected, hex.toColor())
    }

    @Test(expected = NumberFormatException::class)
    fun `toColor should throw on invalid hex`() {
        "GGGGGG".toColor()
    }

    @Test(expected = IllegalStateException::class)
    fun `toColor should throw on invalid length`() {
        "123".toColor()
    }
}
