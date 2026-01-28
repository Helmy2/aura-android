package com.example.aura.feature.favorites

import com.example.aura.domain.model.MediaContent

data class FavoritesState(
    val items: List<MediaContent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface FavoritesEffect {
    data class ShowUserMessage(val message: String) : FavoritesEffect
}