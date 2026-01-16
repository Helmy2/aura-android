package com.example.aura.feature.videos.detail

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.util.StateViewModel
import com.example.aura.shared.core.util.VideoDownloader
import com.example.aura.shared.navigation.AppNavigator
import kotlinx.coroutines.launch

class VideoDetailViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
    private val videoDownloader: VideoDownloader
) : StateViewModel<VideoDetailState>(VideoDetailState()) {

    fun loadVideo(video: Video) {
        updateState { it.copy(video = video, isLoading = false) }
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onDownloadClicked() {
        val currentVideo = currentState.video ?: return

        updateState { it.copy(isDownloading = true) }

        try {
            videoDownloader.downloadVideo(currentVideo.videoUrl, "aura_video_${currentVideo.id}")
            updateState { it.copy(isDownloading = false, userMessage = "Download started") }
        } catch (_: Exception) {
            updateState { it.copy(isDownloading = false, userMessage = "Download failed") }
        }
    }

    fun onToggleFavorite() {
        val currentVideo = currentState.video ?: return

        val newStatus = !currentVideo.isFavorite
        updateState { it.copy(video = currentVideo.copy(isFavorite = newStatus)) }

        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(currentVideo)
            } catch (_: Exception) {
                updateState {
                    it.copy(
                        video = currentVideo.copy(isFavorite = !newStatus),
                        userMessage = "Failed to update favorite"
                    )
                }
            }
        }
    }

    fun onMessageShown() {
        updateState { it.copy(userMessage = null) }
    }
}
