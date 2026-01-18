package com.example.aura.feature.settings

sealed class SettingsSideEffect {
    data class ShowSnackbar(val message: String) : SettingsSideEffect()
}
