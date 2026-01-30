package com.example.aura.feature.settings

import androidx.compose.runtime.Immutable
import com.example.aura.domain.model.ThemeMode

@Immutable
data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface SettingsEffect {
    data class ShowError(val message: String) : SettingsEffect
    data class ShowMessage(val message: String) : SettingsEffect
}
