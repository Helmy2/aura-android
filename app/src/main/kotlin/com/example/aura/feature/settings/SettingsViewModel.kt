package com.example.aura.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ContainerHost<SettingsState, SettingsEffect>, ViewModel() {

    override val container = container<SettingsState, SettingsEffect>(SettingsState())

    fun onCreate() = intent {
        settingsRepository.observeThemeMode()
            .onEach { mode ->
                reduce { state.copy(themeMode = mode, isLoading = false, error = null) }
            }
            .catch { e ->
                reduce { state.copy(isLoading = false, error = e.message) }
                postSideEffect(
                    SettingsEffect.ShowError(e.message ?: "Failed to load theme settings")
                )
            }
            .launchIn(viewModelScope)
    }

    fun onThemeSelected(mode: ThemeMode) = intent {
        try {
            reduce { state.copy(themeMode = mode) }

            settingsRepository.updateThemeMode(mode)

            postSideEffect(
                SettingsEffect.ShowMessage("Theme updated successfully")
            )
        } catch (e: Exception) {
            val currentMode = settingsRepository.observeThemeMode().first()
            reduce { state.copy(themeMode = currentMode, error = e.message) }

            postSideEffect(
                SettingsEffect.ShowError(e.message ?: "Failed to update theme")
            )
        }
    }
}