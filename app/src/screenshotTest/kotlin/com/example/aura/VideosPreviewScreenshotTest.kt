package com.example.aura

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.aura.domain.model.Video
import com.example.aura.domain.model.VideoFile
import com.example.aura.domain.model.User
import com.example.aura.feature.videos.list.VideosScreenContent
import com.example.aura.feature.videos.list.VideosState
import com.example.aura.shared.theme.AuraTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenContentScreenshotTest() {
    val dummyVideo = Video(
        id = 1,
        width = 1920,
        height = 1080,
        duration = 15,
        url = "https://example.com/video.mp4",
        image = "https://example.com/thumb.jpg",
        user = User(id = 1, name = "Jane Doe", url = ""),
        videoFiles = listOf(VideoFile(id = 1, quality = "hd", fileType = "mp4", width = 1920, height = 1080, link = "", fps = 30.0)),
        videoPictures = emptyList(),
        isFavorite = false,
        addedAt = 0L
    )

    AuraTheme {
        VideosScreenContent(
            state = VideosState(
                popularVideos = listOf(dummyVideo)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenLoadingScreenshotTest() {
    AuraTheme {
        VideosScreenContent(
            state = VideosState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenPaginationLoadingScreenshotTest() {
    val dummyVideo = Video(
        id = 1,
        width = 1920,
        height = 1080,
        duration = 15,
        url = "https://example.com/video.mp4",
        image = "https://example.com/thumb.jpg",
        user = User(id = 1, name = "Jane Doe", url = ""),
        videoFiles = listOf(VideoFile(id = 1, quality = "hd", fileType = "mp4", width = 1920, height = 1080, link = "", fps = 30.0)),
        videoPictures = emptyList(),
        isFavorite = false,
        addedAt = 0L
    )

    AuraTheme {
        VideosScreenContent(
            state = VideosState(
                popularVideos = listOf(dummyVideo),
                isPaginationLoading = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenEmptyScreenshotTest() {
    AuraTheme {
        VideosScreenContent(
            state = VideosState(popularVideos = emptyList(), isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenErrorScreenshotTest() {
    AuraTheme {
        VideosScreenContent(
            state = VideosState(error = "API Error"),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun VideosScreenSearchModeScreenshotTest() {
    val dummyVideo = Video(
        id = 2,
        width = 1920,
        height = 1080,
        duration = 15,
        url = "https://example.com/search_video.mp4",
        image = "https://example.com/search_thumb.jpg",
        user = User(id = 2, name = "Search User", url = ""),
        videoFiles = emptyList(),
        videoPictures = emptyList(),
        isFavorite = false,
        addedAt = 0L
    )

    AuraTheme {
        VideosScreenContent(
            state = VideosState(
                isSearchMode = true,
                searchVideos = listOf(dummyVideo)
            ),
            snackbarHostState = remember { SnackbarHostState() },
            searchState = rememberTextFieldState(),
            onBackClick = {},
            onVideoClick = {},
            onFavoriteClick = {},
            onSearchTriggered = {},
            onClearSearch = {}
        )
    }
}
