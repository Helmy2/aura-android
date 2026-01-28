package com.example.aura

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.feature.home.HomeScreen
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.theme.AuraTheme

@PreviewTest
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun HomeScreenPreview() {
    val appNavigator = AppNavigator()
    AuraTheme {
        HomeScreen(appNavigator)
    }
}