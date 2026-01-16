package com.example.aura.feature.settings

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import com.example.aura.shared.core.util.StateViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : StateViewModel<SettingsState>(SettingsState()) {

    init {
        observeThemeMode()
    }

    private fun observeThemeMode() {
        settingsRepository.observeThemeMode()
            .onEach { mode ->
                updateState { it.copy(themeMode = mode, isLoading = false, error = null) }
            }
            .catch { e ->
                updateState { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                updateState { it.copy(themeMode = mode) }

                settingsRepository.updateThemeMode(mode)
            } catch (e: Exception) {
                updateState { it.copy(error = e.message ?: "Failed to update theme") }
            }
        }
    }
}
