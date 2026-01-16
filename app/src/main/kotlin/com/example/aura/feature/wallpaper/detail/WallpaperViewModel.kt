package com.example.aura.feature.wallpaper.detail

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.util.StateViewModel
import com.example.aura.shared.core.util.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator
import kotlinx.coroutines.launch

class WallpaperViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val imageDownloader: ImageDownloader,
    private val navigator: AppNavigator
) : StateViewModel<WallpaperDetailState>(WallpaperDetailState()) {

    fun loadWallpaper(wallpaper: Wallpaper) {
        if (currentState.wallpaper?.id != wallpaper.id) {
            updateState {
                it.copy(
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

    fun onDownloadClicked() {
        val wallpaper = currentState.wallpaper ?: return

        updateState { it.copy(isDownloading = true) }

        viewModelScope.launch {
            val fileName = "aura_${wallpaper.id}"
            val success = imageDownloader.downloadImage(wallpaper.imageUrl, fileName)

            updateState {
                it.copy(
                    isDownloading = false,
                    userMessage = if (success) "Download finished" else "Download failed"
                )
            }
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) {
        val newStatus = !wallpaper.isFavorite
        updateState { state ->
            state.copy(wallpaper = wallpaper.copy(isFavorite = newStatus))
        }

        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(wallpaper)
            } catch (_: Exception) {
                updateState { state ->
                    state.copy(
                        wallpaper = wallpaper.copy(isFavorite = !newStatus),
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
