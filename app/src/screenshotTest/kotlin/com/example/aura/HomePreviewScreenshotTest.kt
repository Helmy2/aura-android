package com.example.aura

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.feature.home.HomeScreen
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.theme.AuraTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val appNavigator = AppNavigator()
    AuraTheme {
        HomeScreen(appNavigator)
    }
}