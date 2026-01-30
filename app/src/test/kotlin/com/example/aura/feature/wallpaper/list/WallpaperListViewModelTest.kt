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
import org.junit.Before
import org.junit.Test
import org.orbitmvi.orbit.test.test

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

        WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator).test(this) {
            containerHost.onCreate()

            expectState {
                copy(wallpapers = wallpapers, isLoading = false)
            }

            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `onSearchTriggered should update state and perform search`() = runTest {
        val query = "nature"
        val results = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 2L })
        coEvery { wallpaperRepository.getCuratedWallpapers(1) } returns listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 1L })
        coEvery { wallpaperRepository.searchWallpapers(query, 1) } returns results

        WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator).test(this) {
            containerHost.onSearchTriggered(query)

            expectState {
                copy(
                    isSearchMode = true,
                    isLoading = true,
                    isEndReached = false,
                    currentPage = 1,
                    searchQuery = query,
                    searchWallpapers = emptyList()
                )
            }
            expectState {
                copy(
                     searchWallpapers = results,
                     isLoading = false,
                     isPaginationLoading = false,
                     currentPage = 1,
                     isEndReached = results.isEmpty()
                )
            }
        }
    }

    @Test
    fun `onLoadNextPage should append wallpapers`() = runTest {
        val initial = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 1L })
        val next = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 2L })
        coEvery { wallpaperRepository.getCuratedWallpapers(2) } returns next

        val initialState = WallpaperListState(wallpapers = initial, currentPage = 1, isLoading = false)

        WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator).test(this, initialState = initialState) {
            containerHost.onLoadNextPage()

            expectState {
                copy(isPaginationLoading = true)
            }

            expectState {
                copy(wallpapers = initial + next, currentPage = 2, isPaginationLoading = false)
            }
        }
    }

    @Test
    fun `onClearSearch should reset search mode`() = runTest {
        val query = "nature"
        val initial = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 1L })
        val results = listOf(mockk<Wallpaper>(relaxed = true) { every { id } returns 2L })
        
        val initialState = WallpaperListState(
            wallpapers = initial,
            isSearchMode = true,
            searchQuery = query,
            searchWallpapers = results,
            currentPage = 1
        )

        WallpaperListViewModel(wallpaperRepository, favoritesRepository, navigator).test(this, initialState = initialState) {
            containerHost.onClearSearch()

            expectState {
                copy(isSearchMode = false, searchQuery = "")
            }
        }
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
        advanceUntilIdle() // Wait for intent to execute

        coVerify { favoritesRepository.toggleFavorite(wallpaper) }
    }
}
