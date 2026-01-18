package com.example.aura.feature.videos.list

sealed class VideosSideEffect {
    data class ShowSnackbar(val message: String) : VideosSideEffect()
}
