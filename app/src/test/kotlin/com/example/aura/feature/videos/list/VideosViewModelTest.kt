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
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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

        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(1, state.popularVideos.size)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `onSearchTriggered should update state and perform search`() = runTest {
        val query = "cars"
        val videos = listOf(mockk<Video>(relaxed = true) { every { id } returns 2L })
        coEvery { videoRepository.searchVideos(query, 1) } returns videos

        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onSearchTriggered(query)
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(true, state.isSearchMode)
        assertEquals(query, state.searchQuery)
        assertEquals(1, state.searchVideos.size)
        assertEquals(2L, state.searchVideos[0].id)
    }

    @Test
    fun `onLoadNextPage in normal mode should append videos`() = runTest {
        val initialVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 1L })
        val nextVideos = listOf(mockk<Video>(relaxed = true) { every { id } returns 2L })
        coEvery { videoRepository.getPopularVideos(1) } returns initialVideos
        coEvery { videoRepository.getPopularVideos(2) } returns nextVideos

        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onLoadNextPage()
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(2, state.popularVideos.size)
        assertEquals(2, state.currentPage)
    }

    @Test
    fun `onClearSearch should reset search mode`() = runTest {
        val viewModel = VideosViewModel(videoRepository, favoritesRepository, navigator)
        advanceUntilIdle()

        viewModel.onSearchTriggered("query")
        advanceUntilIdle()
        
        viewModel.onClearSearch()
        advanceUntilIdle()

        val state = viewModel.container.stateFlow.value
        assertEquals(false, state.isSearchMode)
        assertEquals("", state.searchQuery)
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
        advanceUntilIdle()

        coVerify { favoritesRepository.toggleFavorite(video) }
    }
}
