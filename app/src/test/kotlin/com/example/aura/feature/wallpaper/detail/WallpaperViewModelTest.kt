package com.example.aura.feature.wallpaper.detail

import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

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

        WallpaperViewModel(favoritesRepository, imageDownloader, navigator).test(this) {
            containerHost.loadWallpaper(wallpaper)

            expectState {
                copy(wallpaper = wallpaper, isLoading = false, error=null)
            }
        }
    }

    @Test
    fun `onDownloadClicked should update state and call downloader and show success message`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { imageUrl } returns "http://example.com/image.jpg"
        }
        coEvery { imageDownloader.downloadImage(any(), any()) } returns true

        WallpaperViewModel(favoritesRepository, imageDownloader, navigator).test(this) {
            containerHost.loadWallpaper(wallpaper)

            expectState {
                copy(wallpaper = wallpaper, isLoading = false, error=null)
            }

            containerHost.onDownloadClicked()

            expectState {
                copy(isDownloading = true)
            }
            expectState {
                copy(isDownloading = false)
            }
            expectSideEffect(WallpaperDetailEffect.ShowMessage("Download finished"))
            coVerify { imageDownloader.downloadImage(any(), any()) }
        }
    }

    @Test
    fun `onDownloadClicked should show error message on failure`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { imageUrl } returns "http://example.com/image.jpg"
        }
        coEvery { imageDownloader.downloadImage(any(), any()) } returns false

        WallpaperViewModel(favoritesRepository, imageDownloader, navigator).test(this) {
            containerHost.loadWallpaper(wallpaper)

            expectState {
                copy(wallpaper = wallpaper, isLoading = false, error=null)
            }

            containerHost.onDownloadClicked()

            expectState {
                copy(isDownloading = true)
            }
            expectState {
                copy(isDownloading = false)
            }
            expectSideEffect(WallpaperDetailEffect.ShowError("Download failed"))
        }
    }

    @Test
    fun `onToggleFavorite should call repository`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        WallpaperViewModel(favoritesRepository, imageDownloader, navigator).test(this) {
            containerHost.loadWallpaper(wallpaper)

            expectState {
                copy(wallpaper = wallpaper, isLoading = false, error=null)
            }

            containerHost.onToggleFavorite(wallpaper)

            expectState {
                copy(wallpaper = wallpaper.copy(isFavorite = true))
            }
            
            coVerify { favoritesRepository.toggleFavorite(any<Wallpaper>()) }
        }
    }

    @Test
    fun `onToggleFavorite should rollback state and show error message on failure`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        coEvery { favoritesRepository.toggleFavorite(any<Wallpaper>()) } throws Exception("Failed")

        WallpaperViewModel(favoritesRepository, imageDownloader, navigator).test(this) {
            containerHost.loadWallpaper(wallpaper)

            expectState {
                copy(wallpaper = wallpaper, isLoading = false, error=null)
            }

            containerHost.onToggleFavorite(wallpaper)

            expectState {
                copy(wallpaper = wallpaper.copy(isFavorite = true))
            }
            expectState {
                copy(wallpaper = wallpaper.copy(isFavorite = false))
            }
            expectSideEffect(WallpaperDetailEffect.ShowError("Failed to update favorite"))
        }
    }

    @Test
    fun `onBackClicked should navigate back`() {
        val viewModel = WallpaperViewModel(favoritesRepository, imageDownloader, navigator)
        viewModel.onBackClicked()
        verify { navigator.back() }
    }
}
