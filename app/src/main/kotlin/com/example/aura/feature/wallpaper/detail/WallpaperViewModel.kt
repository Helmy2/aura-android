package com.example.aura.feature.wallpaper.detail

import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.data.downloader.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class WallpaperViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val imageDownloader: ImageDownloader,
    private val navigator: AppNavigator
) : ContainerHost<WallpaperDetailState, WallpaperDetailEffect>, ViewModel() {

    override val container = container<WallpaperDetailState, WallpaperDetailEffect>(WallpaperDetailState())

    fun loadWallpaper(wallpaper: Wallpaper) = intent {
        if (state.wallpaper?.id != wallpaper.id) {
            reduce {
                state.copy(
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

        reduce { state.copy(isDownloading = true) }

        val fileName = "aura_${wallpaper.id}"
        val success = imageDownloader.downloadImage(wallpaper.imageUrl, fileName)

        reduce { state.copy(isDownloading = false) }

        if (success) {
            postSideEffect(WallpaperDetailEffect.ShowMessage("Download finished"))
        } else {
            postSideEffect(WallpaperDetailEffect.ShowError("Download failed"))
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) = intent {
        val newStatus = !wallpaper.isFavorite

        reduce {
            state.copy(wallpaper = wallpaper.copy(isFavorite = newStatus))
        }

        try {
            favoritesRepository.toggleFavorite(wallpaper)
        } catch (_: Exception) {
            reduce { state.copy(wallpaper = wallpaper.copy(isFavorite = !newStatus)) }
            postSideEffect(WallpaperDetailEffect.ShowError("Failed to update favorite"))
        }
    }
}
