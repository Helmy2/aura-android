package com.example.aura.feature.settings

import app.cash.turbine.test
import com.example.aura.domain.model.ThemeMode
import com.example.aura.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

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
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        assertEquals(ThemeMode.SYSTEM, viewModel.container.stateFlow.value.themeMode)
        assertEquals(false, viewModel.container.stateFlow.value.isLoading)
    }

    @Test
    fun `onThemeSelected should update state and repository and show success message`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onThemeSelected(ThemeMode.DARK)
            
            val effect = awaitItem()
            assertEquals(ThemeMode.DARK, viewModel.container.stateFlow.value.themeMode)
            assert(effect is SettingsEffect.ShowMessage)
            coVerify { settingsRepository.updateThemeMode(ThemeMode.DARK) }
        }
    }

    @Test
    fun `onThemeSelected should rollback state and show error message on failure`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository)
        advanceUntilIdle()

        val errorMessage = "Failed to save"
        coEvery { settingsRepository.updateThemeMode(any()) } throws Exception(errorMessage)
        coEvery { settingsRepository.observeThemeMode() } returns flowOf(ThemeMode.LIGHT)

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onThemeSelected(ThemeMode.DARK)
            
            val effect = awaitItem()
            // Should be LIGHT because of rollback (observeThemeMode returns LIGHT in this mock)
            assertEquals(ThemeMode.LIGHT, viewModel.container.stateFlow.value.themeMode)
            assert(effect is SettingsEffect.ShowError)
            assertEquals(errorMessage, (effect as SettingsEffect.ShowError).message)
        }
    }

    @Test
    fun `observeThemeMode should handle repository error`() = runTest {
        val errorMessage = "Failed to load"
        coEvery { settingsRepository.observeThemeMode() } returns flow { throw Exception(errorMessage) }

        val viewModel = SettingsViewModel(settingsRepository)
        
        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            advanceUntilIdle()
            
            val effect = awaitItem()
            assertEquals(false, viewModel.container.stateFlow.value.isLoading)
            assertEquals(errorMessage, viewModel.container.stateFlow.value.error)
            assert(effect is SettingsEffect.ShowError)
            assertEquals(errorMessage, (effect as SettingsEffect.ShowError).message)
        }
    }
}
