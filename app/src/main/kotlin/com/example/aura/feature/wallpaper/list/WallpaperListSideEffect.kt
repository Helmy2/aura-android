package com.example.aura.feature.wallpaper.list

sealed class WallpaperListSideEffect {
    data class ShowSnackbar(val message: String) : WallpaperListSideEffect()
}
