package com.example.aura.feature.videos.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.mvi.ContainerHost
import com.example.aura.shared.core.mvi.ContainerSettings
import com.example.aura.shared.core.mvi.container
import com.example.aura.shared.core.mvi.intent
import com.example.aura.shared.data.downloader.VideoDownloader
import com.example.aura.shared.navigation.AppNavigator

class VideoDetailViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
    private val videoDownloader: VideoDownloader
) : ContainerHost<VideoDetailState, VideoDetailEffect>, ViewModel() {

    override val container = container<VideoDetailState, VideoDetailEffect>(
        initialState = VideoDetailState(),
        settings = ContainerSettings(
            exceptionHandler = { throwable ->
                Log.e(TAG, "Unhandled error: $throwable")
                intent {
                    postSideEffect(VideoDetailEffect.ShowError("An unexpected error occurred"))
                }
            }
        )
    )

    fun loadVideo(video: Video) = intent {
        reduce { copy(video = video, isLoading = false) }
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onDownloadClicked() = intent {
        val currentVideo = state.video ?: return@intent

        reduce { copy(isDownloading = true) }

        try {
            videoDownloader.downloadVideo(currentVideo.videoUrl, "aura_video_${currentVideo.id}")
            reduce { copy(isDownloading = false) }
            postSideEffect(VideoDetailEffect.ShowMessage("Download started"))
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for video: ${currentVideo.id}", e)
            reduce { copy(isDownloading = false) }
            postSideEffect(VideoDetailEffect.ShowError("Download failed"))
        }
    }

    fun onToggleFavorite() = intent {
        val currentVideo = state.video ?: return@intent
        val newStatus = !currentVideo.isFavorite

        reduce { copy(video = currentVideo.copy(isFavorite = newStatus)) }

        try {
            favoritesRepository.toggleFavorite(currentVideo)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite for video: ${currentVideo.id}", e)
            reduce {
                copy(video = currentVideo.copy(isFavorite = !newStatus))
            }
            postSideEffect(VideoDetailEffect.ShowError("Failed to update favorite"))
        }
    }

    companion object {
        private const val TAG = "VideoDetailViewModel"
    }
}
