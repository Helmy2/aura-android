package com.example.aura.feature.wallpaper.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.mvi.ContainerHost
import com.example.aura.shared.core.mvi.ContainerSettings
import com.example.aura.shared.core.mvi.container
import com.example.aura.shared.core.mvi.intent
import com.example.aura.shared.data.downloader.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator

class WallpaperViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val imageDownloader: ImageDownloader,
    private val navigator: AppNavigator
) : ContainerHost<WallpaperDetailState, WallpaperDetailEffect>, ViewModel() {

    override val container = container<WallpaperDetailState, WallpaperDetailEffect>(
        initialState = WallpaperDetailState(),
        settings = ContainerSettings(
            exceptionHandler = { throwable ->
                Log.e(TAG, "Unhandled error: $throwable")
                intent {
                    postSideEffect(WallpaperDetailEffect.ShowError("An unexpected error occurred"))
                }
            }
        )
    )

    fun loadWallpaper(wallpaper: Wallpaper) = intent {
        if (state.wallpaper?.id != wallpaper.id) {
            reduce {
                copy(
                    wallpaper = wallpaper,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onDownloadClicked() = intent {
        val wallpaper = state.wallpaper ?: return@intent

        reduce { copy(isDownloading = true) }

        val fileName = "aura_${wallpaper.id}"
        val success = imageDownloader.downloadImage(wallpaper.imageUrl, fileName)

        reduce { copy(isDownloading = false) }

        if (success) {
            postSideEffect(WallpaperDetailEffect.ShowMessage("Download finished"))
        } else {
            postSideEffect(WallpaperDetailEffect.ShowError("Download failed"))
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) = intent {
        val newStatus = !wallpaper.isFavorite

        reduce {
            copy(wallpaper = wallpaper.copy(isFavorite = newStatus))
        }

        try {
            favoritesRepository.toggleFavorite(wallpaper)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite for wallpaper: ${wallpaper.id}", e)
            reduce {
                copy(wallpaper = wallpaper.copy(isFavorite = !newStatus))
            }
            postSideEffect(WallpaperDetailEffect.ShowError("Failed to update favorite"))
        }
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
    }
}
