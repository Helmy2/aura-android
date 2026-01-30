package com.example.aura.feature.settings

import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        coEvery { settingsRepository.observeThemeMode() } returns flowOf(ThemeMode.SYSTEM)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should observe theme mode`() = runTest {
        SettingsViewModel(settingsRepository).test(this) {
            containerHost.onCreate()

            expectState {
                copy(themeMode = ThemeMode.SYSTEM, isLoading = false)
            }
        }
    }

    @Test
    fun `onThemeSelected should update state and repository and show success message`() = runTest {
        SettingsViewModel(settingsRepository).test(this) {
            containerHost.onThemeSelected(ThemeMode.DARK)

            expectState {
                copy(themeMode = ThemeMode.DARK)
            }
            expectSideEffect(SettingsEffect.ShowMessage("Theme updated successfully"))
        }
    }

    @Test
    fun `onThemeSelected should rollback state and show error message on failure`() = runTest {
        val errorMessage = "Failed to save"
        coEvery { settingsRepository.updateThemeMode(any()) } throws Exception(errorMessage)
        coEvery { settingsRepository.observeThemeMode() } returns flowOf(ThemeMode.LIGHT)

        SettingsViewModel(settingsRepository).test(this) {
            containerHost.onThemeSelected(ThemeMode.DARK)

            expectState { copy(themeMode = ThemeMode.DARK) }
            expectState { copy(themeMode = ThemeMode.LIGHT, error = errorMessage) }
            expectSideEffect(SettingsEffect.ShowError(errorMessage))
        }
    }

    @Test
    fun `observeThemeMode should handle repository error`() = runTest {
        val errorMessage = "Failed to load"
        coEvery { settingsRepository.observeThemeMode() } returns flow {
            throw Exception(errorMessage)
        }

        SettingsViewModel(settingsRepository).test(this) {
            containerHost.onCreate()

            expectState {
                copy(isLoading = false, error = errorMessage)
            }
            expectSideEffect(SettingsEffect.ShowError(errorMessage))
        }
    }
}
