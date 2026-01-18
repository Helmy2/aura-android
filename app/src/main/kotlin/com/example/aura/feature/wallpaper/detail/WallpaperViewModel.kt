package com.example.aura.feature.wallpaper.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.util.ImageDownloader
import com.example.aura.shared.navigation.AppNavigator
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class WallpaperViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val imageDownloader: ImageDownloader,
    private val navigator: AppNavigator
) : ViewModel(), ContainerHost<WallpaperDetailState, WallpaperDetailSideEffect> {

    override val container = container<WallpaperDetailState, WallpaperDetailSideEffect>(WallpaperDetailState())

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
        state.wallpaper?.let { wallpaper ->
            reduce { state.copy(isDownloading = true) }

            viewModelScope.launch {
                val fileName = "aura_${wallpaper.id}"
                val success = imageDownloader.downloadImage(wallpaper.imageUrl, fileName)

                intent {
                    reduce { state.copy(isDownloading = false) }
                    postSideEffect(
                        WallpaperDetailSideEffect.ShowSnackbar(
                            if (success) "Download finished" else "Download failed"
                        )
                    )
                }
            }
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) = intent {
        val newStatus = !wallpaper.isFavorite
        reduce { state.copy(wallpaper = wallpaper.copy(isFavorite = newStatus)) }

        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(wallpaper)
            } catch (_: Exception) {
                intent {
                    reduce { state.copy(wallpaper = wallpaper.copy(isFavorite = !newStatus)) }
                    postSideEffect(WallpaperDetailSideEffect.ShowSnackbar("Failed to update favorite"))
                }
            }
        }
    }
}
