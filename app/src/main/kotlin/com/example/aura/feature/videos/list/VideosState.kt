package com.example.aura.feature.videos.list

import androidx.compose.runtime.Immutable
import com.example.aura.domain.model.Video

@Immutable
data class VideosState(
    val popularVideos: List<Video> = emptyList(),
    val searchVideos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndReached: Boolean = false,
    val isSearchMode: Boolean = false,
    val searchQuery: String = ""
)

sealed interface VideosEffect {
    data class ShowError(val message: String) : VideosEffect
    data class ShowMessage(val message: String) : VideosEffect
}
