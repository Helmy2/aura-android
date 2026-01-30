package com.example.aura.feature.favorites

import com.example.aura.domain.model.MediaContent
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
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
class FavoritesViewModelTest {

    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
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
    fun `init should observe favorites`() = runTest {
        val favorites = listOf(mockk<MediaContent.WallpaperContent>(relaxed = true))
        coEvery { favoritesRepository.observeFavorites() } returns flowOf(favorites)

        FavoritesViewModel(favoritesRepository, navigator).test(this) {
            containerHost.onCreate()

            expectState {
                copy(items = favorites, isLoading = false)
            }
        }
    }

    @Test
    fun `onRemoveFavorite should post side effect on error`() = runTest {
        val item = mockk<MediaContent.WallpaperContent>(relaxed = true)
        val errorMessage = "Removal failed"
        coEvery { favoritesRepository.removeFromFavorite(item) } throws Exception(errorMessage)

        FavoritesViewModel(favoritesRepository, navigator).test(this) {
            containerHost.onRemoveFavorite(item)

            expectSideEffect(FavoritesEffect.ShowUserMessage(errorMessage))
        }
    }

    @Test
    fun `onItemClicked with WallpaperContent should navigate to WallpaperDetail`() {
        val wallpaper = mockk<com.example.aura.domain.model.Wallpaper>(relaxed = true)
        val item = MediaContent.WallpaperContent(wallpaper)
        val viewModel = FavoritesViewModel(favoritesRepository, navigator)

        viewModel.onItemClicked(item)

        verify { navigator.navigate(Destination.WallpaperDetail(wallpaper)) }
    }

    @Test
    fun `onItemClicked with VideoContent should navigate to VideoDetail`() {
        val video = mockk<com.example.aura.domain.model.Video>(relaxed = true)
        val item = MediaContent.VideoContent(video)
        val viewModel = FavoritesViewModel(favoritesRepository, navigator)

        viewModel.onItemClicked(item)

        verify { navigator.navigate(Destination.VideoDetail(video)) }
    }

    @Test
    fun `observeFavorites should handle repository error`() = runTest {
        val errorMessage = "Stream error"
        coEvery { favoritesRepository.observeFavorites() } returns flow { throw Exception(errorMessage) }

        FavoritesViewModel(favoritesRepository, navigator).test(this) {
            containerHost.onCreate()

            expectState {
                copy(isLoading = false, error = errorMessage)
            }
        }
    }
}