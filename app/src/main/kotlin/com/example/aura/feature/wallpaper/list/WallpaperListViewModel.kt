package com.example.aura.feature.wallpaper.list

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.WallpaperRepository
import com.example.aura.shared.core.util.StateViewModel
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WallpaperListViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : StateViewModel<WallpaperListState>(WallpaperListState()) {

    init {
        loadWallpapers(page = 1)
        observeFavorites()
    }

    private fun loadWallpapers(page: Int) {
        viewModelScope.launch {
            try {
                val newWallpapers = wallpaperRepository.getCuratedWallpapers(page = page)

                updateState { state ->
                    val combinedWallpapers = if (page == 1) newWallpapers else state.wallpapers + newWallpapers
                    state.copy(
                        wallpapers = combinedWallpapers,
                        isLoading = false,
                        isPaginationLoading = false,
                        currentPage = page,
                        isEndReached = newWallpapers.isEmpty()
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(isLoading = false, isPaginationLoading = false, error = e.message)
                }
            }
        }
    }

    private fun performSearch(query: String, page: Int) {
        viewModelScope.launch {
            try {
                val results = wallpaperRepository.searchWallpapers(query, page)

                updateState { state ->
                    val combinedResults = if (page == 1) results else state.searchWallpapers + results
                    state.copy(
                        searchWallpapers = combinedResults,
                        isLoading = false,
                        isPaginationLoading = false,
                        currentPage = page,
                        isEndReached = results.isEmpty()
                    )
                }
            } catch (_: Exception) {
                updateState {
                    it.copy(isLoading = false, isPaginationLoading = false, userMessage = "Search failed")
                }
            }
        }
    }

    fun onWallpaperClicked(wallpaper: Wallpaper) {
        navigator.navigate(Destination.WallpaperDetail(wallpaper))
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onLoadNextPage() {
        if (currentState.isPaginationLoading || currentState.isEndReached) return

        val nextPage = currentState.currentPage + 1
        updateState { it.copy(isPaginationLoading = true) }

        if (currentState.isSearchMode) {
            performSearch(currentState.searchQuery, nextPage)
        } else {
            loadWallpapers(nextPage)
        }
    }

    fun onSearchQueryChanged(query: String) {
        updateState { it.copy(searchQuery = query) }
    }

    fun onSearchTriggered(query: String) {
        if (query.isBlank()) return

        updateState {
            it.copy(
                isSearchMode = true,
                isLoading = true,
                isEndReached = false,
                currentPage = 1,
                searchWallpapers = emptyList()
            )
        }
        performSearch(query, 1)
    }

    fun onClearSearch() {
        updateState {
            it.copy(
                isSearchMode = false,
                searchQuery = "",
                isEndReached = false,
                currentPage = 1
            )
        }
        if (currentState.wallpapers.isEmpty()) {
            loadWallpapers(1)
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(wallpaper)
            } catch (_: Exception) {
                updateState { it.copy(userMessage = "Failed to update favorite") }
            }
        }
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavoritesWallpapers()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                updateState { state ->
                    state.copy(
                        favoriteIds = favoriteIds,
                        wallpapers = state.wallpapers.map { it.copy(isFavorite = it.id in favoriteIds) },
                        searchWallpapers = state.searchWallpapers.map { it.copy(isFavorite = it.id in favoriteIds) }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onMessageShown() {
        updateState { it.copy(userMessage = null) }
    }
}
