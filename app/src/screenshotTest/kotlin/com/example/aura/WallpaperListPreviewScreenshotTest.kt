package com.example.aura

import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.domain.model.Wallpaper
import com.example.aura.feature.wallpaper.list.WallpaperListScreenContent
import com.example.aura.feature.wallpaper.list.WallpaperListState
import com.example.aura.shared.theme.AuraTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenContentScreenshotTest() {
    val dummyWallpaper = Wallpaper(
        id = 1,
        imageUrl = "https://example.com/image.jpg",
        smallImageUrl = "https://example.com/small.jpg",
        photographer = "John Doe",
        photographerUrl = "",
        averageColor = "#000000",
        height = 1920,
        width = 1080,
        isFavorite = true,
        addedAt = 0L
    )

    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(
                wallpapers = listOf(dummyWallpaper)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenLoadingScreenshotTest() {
    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenPaginationLoadingScreenshotTest() {
    val dummyWallpaper = Wallpaper(
        id = 1,
        imageUrl = "https://example.com/image.jpg",
        smallImageUrl = "https://example.com/small.jpg",
        photographer = "John Doe",
        photographerUrl = "",
        averageColor = "#000000",
        height = 1920,
        width = 1080,
        isFavorite = true,
        addedAt = 0L
    )

    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(
                wallpapers = listOf(dummyWallpaper),
                isPaginationLoading = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenEmptyScreenshotTest() {
    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(wallpapers = emptyList(), isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenErrorScreenshotTest() {
    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(error = "Network error"),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun WallpaperListScreenSearchModeScreenshotTest() {
    val dummyWallpaper = Wallpaper(
        id = 2,
        imageUrl = "https://example.com/search_image.jpg",
        smallImageUrl = "https://example.com/search_small.jpg",
        photographer = "Jane Smith",
        photographerUrl = "",
        averageColor = "#FFFFFF",
        height = 1920,
        width = 1080,
        isFavorite = false,
        addedAt = 0L
    )

    AuraTheme {
        WallpaperListScreenContent(
            state = WallpaperListState(
                isSearchMode = true,
                searchWallpapers = listOf(dummyWallpaper)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            listState = rememberLazyStaggeredGridState(),
            onBackClick = {},
            onWallpaperClick = {},
            onToggleFavorite = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}
