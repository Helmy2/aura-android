package com.example.aura.feature.videos.detail

import com.example.aura.domain.model.Video

data class VideoDetailState(
    val video: Video? = null,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val error: String? = null
)
