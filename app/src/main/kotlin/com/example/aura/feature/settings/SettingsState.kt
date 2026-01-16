package com.example.aura.feature.settings

import com.example.aura.domain.model.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true,
    val error: String? = null
)