package com.example.aura

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.domain.model.ThemeMode
import com.example.aura.feature.settings.SettingsScreenContent
import com.example.aura.feature.settings.SettingsState
import com.example.aura.shared.theme.AuraTheme

class SettingsScreenshotTest {

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun SettingsScreenSystemScreenshotTest() {
        AuraTheme {
            SettingsScreenContent(
                state = SettingsState(themeMode = ThemeMode.SYSTEM),
                onThemeSelected = {},
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun SettingsScreenLightScreenshotTest() {
        AuraTheme {
            SettingsScreenContent(
                state = SettingsState(themeMode = ThemeMode.LIGHT),
                onThemeSelected = {},
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }

    @PreviewTest
    @Preview(showBackground = true)
    @Composable
    fun SettingsScreenLoadingScreenshotTest() {
        AuraTheme {
            SettingsScreenContent(
                state = SettingsState(isLoading = true),
                onThemeSelected = {},
                snackbarHostState = remember { SnackbarHostState() }
            )
        }
    }
}
