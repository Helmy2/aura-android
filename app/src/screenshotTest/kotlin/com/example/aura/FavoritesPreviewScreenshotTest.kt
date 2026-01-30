package com.example.aura

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.domain.model.MediaContent
import com.example.aura.domain.model.Wallpaper
import com.example.aura.feature.favorites.FavoritesScreenContent
import com.example.aura.feature.favorites.FavoritesState
import com.example.aura.shared.theme.AuraTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun FavoritesScreenContentScreenshotTest() {
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
        FavoritesScreenContent(
            state = FavoritesState(
                items = listOf(MediaContent.WallpaperContent(dummyWallpaper))
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onItemClick = {},
            onRemoveFavorite = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun FavoritesScreenLoadingScreenshotTest() {
    AuraTheme {
        FavoritesScreenContent(
            state = FavoritesState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onItemClick = {},
            onRemoveFavorite = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun FavoritesScreenEmptyScreenshotTest() {
    AuraTheme {
        FavoritesScreenContent(
            state = FavoritesState(items = emptyList(), isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            onItemClick = {},
            onRemoveFavorite = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun FavoritesScreenErrorScreenshotTest() {
    AuraTheme {
        FavoritesScreenContent(
            state = FavoritesState(error = "Connection timeout"),
            snackbarHostState = remember { SnackbarHostState() },
            onItemClick = {},
            onRemoveFavorite = {}
        )
    }
}
