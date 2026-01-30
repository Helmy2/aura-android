package com.example.aura.feature.videos.detail

import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.VideoDownloader
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

        VideoDetailViewModel(favoritesRepository, navigator, videoDownloader).test(this) {
            containerHost.loadVideo(video)

            expectState {
                copy(video = video, isLoading = false)
            }
        }
    }

    @Test
    fun `onDownloadClicked should update state and call downloader and show success message`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { videoUrl } returns "http://example.com/video.mp4"
        }
        every { videoDownloader.downloadVideo(any(), any()) } returns 123L

        VideoDetailViewModel(favoritesRepository, navigator, videoDownloader).test(this) {
            containerHost.loadVideo(video)

            containerHost.loadVideo(video)
            
            expectState {
                copy(video = video, isLoading = false)
            }

            containerHost.onDownloadClicked()

            expectState {
                copy(isDownloading = true)
            }
            expectState {
                copy(isDownloading = false)
            }
            expectSideEffect(VideoDetailEffect.ShowMessage("Download started"))
            verify { videoDownloader.downloadVideo(any(), any()) }
        }
    }

    @Test
    fun `onDownloadClicked should show error message on failure`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { videoUrl } returns "http://example.com/video.mp4"
        }
        every { videoDownloader.downloadVideo(any(), any()) } throws Exception("Failed")

        VideoDetailViewModel(favoritesRepository, navigator, videoDownloader).test(this) {
            containerHost.loadVideo(video)

            expectState {
                copy(video = video, isLoading = false)
            }

            containerHost.onDownloadClicked()

            expectState {
                copy(isDownloading = true)
            }
            expectState {
                copy(isDownloading = false)
            }
            expectSideEffect(VideoDetailEffect.ShowError("Download failed"))
        }
    }

    @Test
    fun `onToggleFavorite should call repository and update state`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        VideoDetailViewModel(favoritesRepository, navigator, videoDownloader).test(this) {
            containerHost.loadVideo(video)

            expectState {
                copy(video = video, isLoading = false)
            }

            containerHost.onToggleFavorite()

            expectState {
                copy(video = video.copy(isFavorite = true))
            }

            coVerify { favoritesRepository.toggleFavorite(any<Video>()) }
        }
    }

    @Test
    fun `onToggleFavorite should rollback state and show error on failure`() = runTest {
        val video = mockk<Video>(relaxed = true) {
            every { id } returns 1L
            every { isFavorite } returns false
        }
        coEvery { favoritesRepository.toggleFavorite(any<Video>()) } throws Exception("Error")

        VideoDetailViewModel(favoritesRepository, navigator, videoDownloader).test(this) {
            containerHost.loadVideo(video)

            containerHost.loadVideo(video)

            expectState {
                copy(video = video, isLoading = false)
            }

            containerHost.onToggleFavorite()

            expectState {
                copy(video = video.copy(isFavorite = true))
            }
            expectState {
                copy(video = video.copy(isFavorite = false))
            }
            expectSideEffect(VideoDetailEffect.ShowError("Failed to update favorite"))
        }
    }

    @Test
    fun `onBackClicked should navigate back`() {
        val viewModel = VideoDetailViewModel(favoritesRepository, navigator, videoDownloader)
        viewModel.onBackClicked()
        verify { navigator.back() }
    }
}
