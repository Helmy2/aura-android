package com.example.aura.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel(), ContainerHost<SettingsState, SettingsSideEffect> {

    override val container = container<SettingsState, SettingsSideEffect>(SettingsState())

    init {
        observeThemeMode()
    }

    private fun observeThemeMode() = intent {
        settingsRepository.observeThemeMode()
            .onEach { mode ->
                reduce { state.copy(themeMode = mode, isLoading = false, error = null) }
            }
            .catch { e ->
                reduce { state.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onThemeSelected(mode: ThemeMode) = intent {
        viewModelScope.launch {
            try {
                reduce { state.copy(themeMode = mode) }

                settingsRepository.updateThemeMode(mode)
            } catch (e: Exception) {
                postSideEffect(SettingsSideEffect.ShowSnackbar(e.message ?: "Failed to update theme"))
            }
        }
    }
}
