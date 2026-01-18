package com.example.aura.feature.videos.list

import com.example.aura.domain.model.Video

data class VideosState(
    val popularVideos: List<Video> = emptyList(),
    val searchVideos: List<Video> = emptyList(),

    val isLoading: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val error: String? = null,

    val currentPage: Int = 1,
    val isEndReached: Boolean = false,

    val isSearchMode: Boolean = false
)
