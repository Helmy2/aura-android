package com.example.aura.feature.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.MediaContent
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.shared.core.mvi.ContainerHost
import com.example.aura.shared.core.mvi.ContainerSettings
import com.example.aura.shared.core.mvi.container
import com.example.aura.shared.core.mvi.intent
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : ContainerHost<FavoritesState, FavoritesEffect>, ViewModel() {

    override val container = container<FavoritesState, FavoritesEffect>(
        initialState = FavoritesState(),
        settings = ContainerSettings(
            exceptionHandler = { throwable ->
                Log.e("FavoritesViewModel", "Error: $throwable")
                intent {
                    postSideEffect(FavoritesEffect.ShowUserMessage("An error occurred"))
                }
            }
        )
    )

    init {
        observeFavorites()
    }

    private fun observeFavorites() = intent {
        favoritesRepository.observeFavorites()
            .onEach { items ->
                reduce { copy(items = items, isLoading = false, error = null) }
            }
            .catch { e ->
                reduce { copy(isLoading = false, error = e.message) }
            }
            .launchIn(viewModelScope)
    }

    fun onRemoveFavorite(item: MediaContent) = intent {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFromFavorite(item)
            } catch (e: Exception) {
                postSideEffect(FavoritesEffect.ShowUserMessage(e.message ?: "Failed to remove"))
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
}