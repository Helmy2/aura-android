package com.example.aura.feature.videos.detail

import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.VideoDownloader
import com.example.aura.shared.navigation.AppNavigator
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class VideoDetailViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
    private val videoDownloader: VideoDownloader
) : ContainerHost<VideoDetailState, VideoDetailEffect>, ViewModel() {

    override val container = container<VideoDetailState, VideoDetailEffect>(VideoDetailState())

    fun loadVideo(video: Video) = intent {
        reduce { state.copy(video = video, isLoading = false) }
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onDownloadClicked() = intent {
        val currentVideo = state.video ?: return@intent

        reduce { state.copy(isDownloading = true) }

        try {
            videoDownloader.downloadVideo(currentVideo.videoUrl, "aura_video_${currentVideo.id}")
            reduce { state.copy(isDownloading = false) }
            postSideEffect(VideoDetailEffect.ShowMessage("Download started"))
        } catch (_: Exception) {
            reduce { state.copy(isDownloading = false) }
            postSideEffect(VideoDetailEffect.ShowError("Download failed"))
        }
    }

    fun onToggleFavorite() = intent {
        val currentVideo = state.video ?: return@intent
        val newStatus = !currentVideo.isFavorite

        reduce { state.copy(video = currentVideo.copy(isFavorite = newStatus)) }

        try {
            favoritesRepository.toggleFavorite(currentVideo)
        } catch (_: Exception) {
            reduce {
                state.copy(video = currentVideo.copy(isFavorite = !newStatus))
            }
            postSideEffect(VideoDetailEffect.ShowError("Failed to update favorite"))
        }
    }
}
