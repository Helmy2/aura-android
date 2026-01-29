package com.example.aura.feature.wallpaper.detail

import app.cash.turbine.test
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WallpaperViewModelTest {

    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
    private val imageDownloader = mockk<ImageDownloader>(relaxed = true)
    private val navigator = mockk<AppNavigator>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadWallpaper should update state`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true)
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        
        viewModel.loadWallpaper(wallpaper)
        advanceUntilIdle()

        assertEquals(wallpaper, viewModel.container.stateFlow.value.wallpaper)
        assertEquals(false, viewModel.container.stateFlow.value.isLoading)
    }

    @Test
    fun `onDownloadClicked should update state and call downloader and show success message`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { imageUrl } returns "http://example.com/image.jpg"
        }
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        viewModel.loadWallpaper(wallpaper)
        advanceUntilIdle()

        coEvery { imageDownloader.downloadImage(any(), any()) } returns true

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onDownloadClicked()
            
            val effect = awaitItem()
            assert(effect is WallpaperDetailEffect.ShowMessage)
            assertEquals("Download finished", (effect as WallpaperDetailEffect.ShowMessage).message)
            assertEquals(false, viewModel.container.stateFlow.value.isDownloading)
            coVerify { imageDownloader.downloadImage(any(), any()) }
        }
    }

    @Test
    fun `onDownloadClicked should show error message on failure`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { imageUrl } returns "http://example.com/image.jpg"
        }
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        viewModel.loadWallpaper(wallpaper)
        advanceUntilIdle()

        coEvery { imageDownloader.downloadImage(any(), any()) } returns false

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onDownloadClicked()
            
            val effect = awaitItem()
            assert(effect is WallpaperDetailEffect.ShowError)
            assertEquals("Download failed", (effect as WallpaperDetailEffect.ShowError).message)
            assertEquals(false, viewModel.container.stateFlow.value.isDownloading)
        }
    }

    @Test
    fun `onToggleFavorite should call repository`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        
        viewModel.onToggleFavorite(wallpaper)
        advanceUntilIdle()

        coVerify { favoritesRepository.toggleFavorite(any<Wallpaper>()) }
    }

    @Test
    fun `onToggleFavorite should rollback state and show error message on failure`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        coEvery { favoritesRepository.toggleFavorite(any<Wallpaper>()) } throws Exception("Failed")

        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        
        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onToggleFavorite(wallpaper)
            
            val effect = awaitItem()
            assert(effect is WallpaperDetailEffect.ShowError)
            // Should be false because of rollback
            assertEquals(false, viewModel.container.stateFlow.value.wallpaper?.isFavorite)
        }
    }

    @Test
    fun `onBackClicked should navigate back`() {
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        viewModel.onBackClicked()
        verify { navigator.back() }
    }
}
