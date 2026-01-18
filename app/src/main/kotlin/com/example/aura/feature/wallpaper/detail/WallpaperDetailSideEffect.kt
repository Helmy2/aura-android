package com.example.aura.feature.wallpaper.detail

sealed class WallpaperDetailSideEffect {
    data class ShowSnackbar(val message: String) : WallpaperDetailSideEffect()
}
