package com.example.aura.feature.wallpaper.detail

import com.example.aura.domain.model.Wallpaper

data class WallpaperDetailState(
    val wallpaper: Wallpaper? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDownloading: Boolean = false
)

sealed interface WallpaperDetailEffect {
    data class ShowMessage(val message: String) : WallpaperDetailEffect
    data class ShowError(val message: String) : WallpaperDetailEffect
}
