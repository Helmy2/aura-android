package com.example.aura.feature.videos.detail

import app.cash.turbine.test
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.VideoDownloader
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
class VideoDetailViewModelTest {

    private val favoritesRepository = mockk<FavoritesRepository>(relaxed = true)
    private val navigator = mockk<AppNavigator>(relaxed = true)
    private val videoDownloader = mockk<VideoDownloader>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadVideo should update state`() = runTest {
        val video = mockk<Video>(relaxed = true)
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        
        viewModel.loadVideo(video)
        advanceUntilIdle()

        assertEquals(video, viewModel.container.stateFlow.value.video)
        assertEquals(false, viewModel.container.stateFlow.value.isLoading)
    }

    @Test
    fun `onDownloadClicked should update state and call downloader and show success message`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { videoUrl } returns "http://example.com/video.mp4"
        }
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.loadVideo(video)
        advanceUntilIdle()

        every { videoDownloader.downloadVideo(any(), any()) } returns 123L

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onDownloadClicked()
            
            val effect = awaitItem()
            assert(effect is VideoDetailEffect.ShowMessage)
            assertEquals("Download started", (effect as VideoDetailEffect.ShowMessage).message)
            assertEquals(false, viewModel.container.stateFlow.value.isDownloading)
            verify { videoDownloader.downloadVideo(any(), any()) }
        }
    }

    @Test
    fun `onDownloadClicked should show error message on failure`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { videoUrl } returns "http://example.com/video.mp4"
        }
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.loadVideo(video)
        advanceUntilIdle()

        every { videoDownloader.downloadVideo(any(), any()) } throws Exception("Failed")

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onDownloadClicked()
            
            val effect = awaitItem()
            assert(effect is VideoDetailEffect.ShowError)
            assertEquals("Download failed", (effect as VideoDetailEffect.ShowError).message)
            assertEquals(false, viewModel.container.stateFlow.value.isDownloading)
        }
    }

    @Test
    fun `onToggleFavorite should call repository and update state`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.loadVideo(video)
        advanceUntilIdle()

        viewModel.onToggleFavorite()
        advanceUntilIdle()

        coVerify { favoritesRepository.toggleFavorite(any<Video>()) }
    }

    @Test
    fun `onToggleFavorite should rollback state and show error on failure`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        coEvery { favoritesRepository.toggleFavorite(any<Video>()) } throws Exception("Error")

        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.loadVideo(video)
        advanceUntilIdle()

        viewModel.container.sideEffectFlow.test(timeout = 2.seconds) {
            viewModel.onToggleFavorite()
            
            val effect = awaitItem()
            assert(effect is VideoDetailEffect.ShowError)
            // Rollback to false
            assertEquals(false, viewModel.container.stateFlow.value.video?.isFavorite)
        }
    }

    @Test
    fun `onBackClicked should navigate back`() {
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.onBackClicked()
        verify { navigator.back() }
    }
}
