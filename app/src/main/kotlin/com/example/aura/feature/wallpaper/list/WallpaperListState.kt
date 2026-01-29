package com.example.aura.feature.wallpaper.list

import androidx.compose.runtime.Immutable
import com.example.aura.domain.model.Wallpaper

@Immutable
data class WallpaperListState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val searchWallpapers: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = true,
    val isPaginationLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndReached: Boolean = false,
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val favoriteIds: Set<Long> = emptySet()
)

sealed interface WallpaperListEffect {
    data class ShowError(val message: String) : WallpaperListEffect
    data class ShowMessage(val message: String) : WallpaperListEffect
}
