package com.example.aura.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import com.example.aura.shared.core.mvi.ContainerHost
import com.example.aura.shared.core.mvi.ContainerSettings
import com.example.aura.shared.core.mvi.container
import com.example.aura.shared.core.mvi.intent
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ContainerHost<SettingsState, SettingsEffect>, ViewModel() {

    override val container = container<SettingsState, SettingsEffect>(
        initialState = SettingsState(),
        settings = ContainerSettings(
            exceptionHandler = { throwable ->
                Log.e(TAG, "Unhandled error: $throwable")
                intent {
                    postSideEffect(SettingsEffect.ShowError("An unexpected error occurred"))
                }
            }
        )
    )

    init {
        observeThemeMode()
    }

    private fun observeThemeMode() = intent {
        settingsRepository.observeThemeMode()
            .onEach { mode ->
                reduce { copy(themeMode = mode, isLoading = false, error = null) }
            }
            .catch { e ->
                reduce { copy(isLoading = false, error = e.message) }
                postSideEffect(
                    SettingsEffect.ShowError(e.message ?: "Failed to load theme settings")
                )
            }
            .collect()
    }

    fun onThemeSelected(mode: ThemeMode) = intent {
        try {
            reduce { copy(themeMode = mode) }

            settingsRepository.updateThemeMode(mode)

            postSideEffect(
                SettingsEffect.ShowMessage("Theme updated successfully")
            )
        } catch (e: Exception) {
            val currentMode = settingsRepository.observeThemeMode().first()
            reduce { copy(themeMode = currentMode, error = e.message) }

            postSideEffect(
                SettingsEffect.ShowError(e.message ?: "Failed to update theme")
            )
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}
