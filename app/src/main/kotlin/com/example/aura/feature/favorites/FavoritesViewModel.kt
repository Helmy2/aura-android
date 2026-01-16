package com.example.aura.feature.favorites

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.MediaContent
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.util.StateViewModel
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : StateViewModel<FavoritesState>(FavoritesState()) {

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavorites()
            .onEach { items ->
                updateState { it.copy(items = items, isLoading = false, error = null) }
            }
            .catch { e ->
                updateState { it.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onRemoveFavorite(item: MediaContent) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFromFavorite(item)
            } catch (e: Exception) {
                updateState { it.copy(userMessage = e.message ?: "Failed to remove") }
            }
        }
    }

    fun onItemClicked(item: MediaContent) {
        when (item) {
            is MediaContent.VideoContent ->
                navigator.navigate(Destination.VideoDetail(item.video))

            is MediaContent.WallpaperContent ->
                navigator.navigate(Destination.WallpaperDetail(item.wallpaper))
        }
    }

    fun onMessageShown() {
        updateState { it.copy(userMessage = null) }
    }
}