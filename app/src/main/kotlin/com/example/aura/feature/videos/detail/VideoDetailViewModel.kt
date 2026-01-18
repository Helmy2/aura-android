package com.example.aura.feature.videos.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.util.VideoDownloader
import com.example.aura.shared.navigation.AppNavigator
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class VideoDetailViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
    private val videoDownloader: VideoDownloader
) : ViewModel(), ContainerHost<VideoDetailState, VideoDetailSideEffect> {

    override val container = container<VideoDetailState, VideoDetailSideEffect>(VideoDetailState())

    fun loadVideo(video: Video) = intent {
        reduce { state.copy(video = video, isLoading = false) }
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onDownloadClicked() = intent {
        state.video?.let { currentVideo ->
            reduce { state.copy(isDownloading = true) }

            try {
                videoDownloader.downloadVideo(currentVideo.videoUrl, "aura_video_${currentVideo.id}")
                postSideEffect(VideoDetailSideEffect.ShowSnackbar("Download started"))
            } catch (_: Exception) {
                postSideEffect(VideoDetailSideEffect.ShowSnackbar("Download failed"))
            } finally {
                reduce { state.copy(isDownloading = false) }
            }
        }
    }

    fun onToggleFavorite() = intent {
        state.video?.let { currentVideo ->
            val newStatus = !currentVideo.isFavorite
            reduce { state.copy(video = currentVideo.copy(isFavorite = newStatus)) }

            viewModelScope.launch {
                try {
                    favoritesRepository.toggleFavorite(currentVideo)
                } catch (_: Exception) {
                    intent {
                        reduce { state.copy(video = currentVideo.copy(isFavorite = !newStatus)) }
                        postSideEffect(VideoDetailSideEffect.ShowSnackbar("Failed to update favorite"))
                    }
                }
            }
        }
    }
}
