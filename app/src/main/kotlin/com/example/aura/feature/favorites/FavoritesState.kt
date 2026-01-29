package com.example.aura.feature.favorites

import androidx.compose.runtime.Immutable
import com.example.aura.domain.model.MediaContent

@Immutable
data class FavoritesState(
    val items: List<MediaContent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

sealed interface FavoritesEffect {
    data class ShowUserMessage(val message: String) : FavoritesEffect
}