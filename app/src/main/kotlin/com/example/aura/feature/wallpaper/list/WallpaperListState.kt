package com.example.aura.feature.wallpaper.list

import com.example.aura.domain.model.Wallpaper

data class WallpaperListState(
    val wallpapers: List<Wallpaper> = emptyList(),
    val searchWallpapers: List<Wallpaper> = emptyList(),

    val isLoading: Boolean = true,
    val isPaginationLoading: Boolean = false,
    val error: String? = null,

    val currentPage: Int = 1,
    val isEndReached: Boolean = false,

    val isSearchMode: Boolean = false,

    val favoriteIds: Set<Long> = emptySet()
)
