package com.example.aura.feature.videos.detail

sealed class VideoDetailSideEffect {
    data class ShowSnackbar(val message: String) : VideoDetailSideEffect()
}
