package com.example.aura.feature.wallpaper.list

import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.WallpaperRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WallpaperListViewModelTest {

    private val wallpaperRepository = mockk<WallpaperRepository>(relaxed = true)
    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
    private val navigator = mockk<AppNavigator>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { favoritesRepository.observeFavoritesWallpapers() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load wallpapers`() = runTest {
        val wallpapers = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 1L })
        coEvery { wallpaperRepository.getCuratedWallpapers(1) } returns wallpapers

        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(1, state.wallpapers.size)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `onSearchTriggered should update state and perform search`() = runTest {
        val query = "nature"
        val results = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 2L })
        coEvery { wallpaperRepository.searchWallpapers(query, 1) } returns results

        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onSearchTriggered(query)
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(true, state.isSearchMode)
        assertEquals(query, state.searchQuery)
        assertEquals(1, state.searchWallpapers.size)
    }

    @Test
    fun `onLoadNextPage should append wallpapers`() = runTest {
        val initial = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 1L })
        val next = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 2L })
        coEvery { wallpaperRepository.getCuratedWallpapers(1) } returns initial
        coEvery { wallpaperRepository.getCuratedWallpapers(2) } returns next

        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onLoadNextPage()
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(2, state.wallpapers.size)
        assertEquals(2, state.currentPage)
    }

    @Test
    fun `onClearSearch should reset search mode`() = runTest {
        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onSearchTriggered("nature")
        advanceUntilIdle()
        
        viewModel.onClearSearch()
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(false, state.isSearchMode)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun `onWallpaperClicked should navigate to detail`() {
        val wallpaper = mockk<Wallpaper>(relaxed = true)
        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        
        viewModel.onWallpaperClicked(wallpaper)
        
        verify { navigator.navigate(Destination.WallpaperDetail(wallpaper)) }
    }

    @Test
    fun `onToggleFavorite should call repository`() = runTest {
        val wallpaper = mockk<Wallpaper>(relaxed = true)
        val viewModel = WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator)
        
        viewModel.onToggleFavorite(wallpaper)
        advanceUntilIdle()

        coVerify { favoritesRepository.toggleFavorite(wallpaper) }
    }
}
