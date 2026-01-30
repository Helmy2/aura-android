package com.example.aura.shared.core.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class PaddingValuesExtensionsTest {

    @Test
    fun `plus operator should add padding values correctly`() {
        // Given
        val padding1 = PaddingValues(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp)
        val padding2 = PaddingValues(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 5.dp)

        // When
        val result = padding1 + padding2

        // Then
        // Note: calculateStart/EndPadding requires LayoutDirection. 
        // The extension hardcodes Ltr for calculation.
        val layoutDirection = LayoutDirection.Ltr
        
        val expectedStart = 15.dp
        val expectedTop = 25.dp
        val expectedEnd = 35.dp
        val expectedBottom = 45.dp

        assertEquals(expectedStart, result.calculateStartPadding(layoutDirection))
        assertEquals(expectedTop, result.calculateTopPadding())
        assertEquals(expectedEnd, result.calculateEndPadding(layoutDirection))
        assertEquals(expectedBottom, result.calculateBottomPadding())
    }
    
    @Test
    fun `plus operator should handle all-sided padding`() {
        val padding1 = PaddingValues(10.dp)
        val padding2 = PaddingValues(20.dp)
        
        val result = padding1 + padding2
        
        val layoutDirection = LayoutDirection.Ltr
        assertEquals(30.dp, result.calculateStartPadding(layoutDirection))
        assertEquals(30.dp, result.calculateTopPadding())
        assertEquals(30.dp, result.calculateEndPadding(layoutDirection))
        assertEquals(30.dp, result.calculateBottomPadding())
    }
}
