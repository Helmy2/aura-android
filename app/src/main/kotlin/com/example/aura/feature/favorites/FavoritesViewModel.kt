package com.example.aura.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.MediaContent
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : ViewModel(), ContainerHost<FavoritesState, FavoritesSideEffect> {

    override val container = container<FavoritesState, FavoritesSideEffect>(FavoritesState())

    init {
        observeFavorites()
    }

    private fun observeFavorites() = intent {
        favoritesRepository.observeFavorites()
            .onEach { items ->
                reduce { state.copy(items = items, isLoading = false, error = null) }
            }
            .catch { e ->
                reduce { state.copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onRemoveFavorite(item: MediaContent) = intent {
        try {
            favoritesRepository.removeFromFavorite(item)
        } catch (e: Exception) {
            postSideEffect(FavoritesSideEffect.ShowSnackbar(e.message ?: "Failed to remove"))
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
}