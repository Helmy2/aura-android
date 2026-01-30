package com.example.aura.feature.videos.list

import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.VideoRepository
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
class VideosViewModelTest {

    private val videoRepository = mockk<VideoRepository>(relaxed = true)
    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
    private val navigator = mockk<AppNavigator>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        every { favoritesRepository.observeFavoriteVideos() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load popular videos`() = runTest {
        val videos = listOf(mockk<Video>(relaxed = true) { every { id } returns 1L })
        coEvery { videoRepository.getPopularVideos(1) } returns videos

        VideosViewModel(videoRepository, favoritesRepository, navigator).test(this) {
            containerHost.onCreate()
            
            expectState {
                copy(isLoading = true)
            }

            expectState {
                copy(popularVideos = videos, isLoading = false)
            }

            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun `onSearchTriggered should update state and perform search`() = runTest {
        val query = "cars"
        val videos = listOf(mockk<Video>(relaxed = true) { every { id } returns 2L })
        coEvery { videoRepository.getPopularVideos(1) } returns listOf(mockk<Video>(relaxed = true) { every { id } returns 1L })
        coEvery { videoRepository.searchVideos(query, 1) } returns videos

        VideosViewModel(videoRepository, favoritesRepository, navigator).test(this) {
            containerHost.onSearchTriggered(query)

            expectState {
                copy(
                    isSearchMode = true,
                    isLoading = true,
                    isEndReached = false,
                    currentPage = 1,
                    searchQuery = query,
                    searchVideos = emptyList()
                )
            }

            expectState {
                copy(
                    searchVideos = videos,
                    isLoading = false,
                    isPaginationLoading = false,
                    currentPage = 1,
                    isEndReached = videos.isEmpty()
                )
            }
        }
    }

    @Test
    fun `onLoadNextPage in normal mode should append videos`() = runTest {
        val initialVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 1L })
        val nextVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 2L })
        coEvery { videoRepository.getPopularVideos(2) } returns nextVideos

        val initialState = VideosState(popularVideos = initialVideos, currentPage = 1, isLoading = false)

        VideosViewModel(videoRepository, favoritesRepository, navigator).test(this, initialState = initialState) {
            containerHost.onLoadNextPage()

            expectState {
                copy(isPaginationLoading = true)
            }

            expectState {
                copy(popularVideos = initialVideos + nextVideos, currentPage = 2, isPaginationLoading = false)
            }
        }
    }

    @Test
    fun `onClearSearch should reset search mode`() = runTest {
        val query = "query"
        val initialVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 1L })
        val searchVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 2L })

        // Initial state includes search mode active
        val initialState = VideosState(
            popularVideos = initialVideos,
            isSearchMode = true,
            searchQuery = query,
            searchVideos = searchVideos,
            currentPage = 1
        )

        VideosViewModel(videoRepository, favoritesRepository, navigator).test(this, initialState = initialState) {
            containerHost.onClearSearch()

            expectState {
                copy(
                    isSearchMode = false,
                    isEndReached = false,
                    currentPage = 1,
                    searchQuery = ""
                )
            }
        }
    }

    @Test
    fun `onVideoClicked should navigate to detail`() {
        val video = mockk<Video>(relaxed = true)
        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)

        viewModel.onVideoClicked(video)

        verify { navigator.navigate(Destination.VideoDetail(video)) }
    }

    @Test
    fun `onFavoriteClicked should call repository`() = runTest {
        val video = mockk<Video>(relaxed = true)
        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)
        
        viewModel.onFavoriteClicked(video)
        advanceUntilIdle() // Wait for intent to execute
        
        coVerify { favoritesRepository.toggleFavorite(video) }
    }
}
