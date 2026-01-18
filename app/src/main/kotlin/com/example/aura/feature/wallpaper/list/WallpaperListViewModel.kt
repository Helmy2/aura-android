package com.example.aura.feature.wallpaper.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.WallpaperRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class WallpaperListViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : ViewModel(), ContainerHost<WallpaperListState, WallpaperListSideEffect> {

    override val container = container<WallpaperListState, WallpaperListSideEffect>(WallpaperListState())

    private var currentActiveQuery: String = ""

    init {
        loadWallpapers(page = 1)
        observeFavorites()
    }

    private fun loadWallpapers(page: Int) = intent {
        viewModelScope.launch {
            try {
                val newWallpapers = wallpaperRepository.getCuratedWallpapers(page = page)

                reduce { 
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
                reduce { state.copy(isLoading = false, isPaginationLoading = false, error = e.message) }
            }
        }
    }

    private fun performSearch(query: String, page: Int) {
        currentActiveQuery = query

        viewModelScope.launch {
            try {
                val results = wallpaperRepository.searchWallpapers(query, page)

                intent {
                    reduce {
                        val combinedResults = if (page == 1) results else state.searchWallpapers + results
                        state.copy(
                            searchWallpapers = combinedResults,
                            isLoading = false,
                            isPaginationLoading = false,
                            currentPage = page,
                            isEndReached = results.isEmpty()
                        )
                    }
                }
            } catch (_: Exception) {
                intent {
                    postSideEffect(WallpaperListSideEffect.ShowSnackbar("Search failed"))
                    reduce { state.copy(isLoading = false, isPaginationLoading = false) }
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

    fun onLoadNextPage() = intent {
        if (state.isPaginationLoading || state.isEndReached) return@intent

        val nextPage = state.currentPage + 1
        reduce { state.copy(isPaginationLoading = true) }

        if (state.isSearchMode) {
            performSearch(currentActiveQuery, nextPage)
        } else {
            loadWallpapers(nextPage)
        }
    }

    fun onSearchTriggered(query: String) = intent {
        if (query.isBlank()) return@intent

        reduce {
            state.copy(
                isSearchMode = true,
                isLoading = true,
                isEndReached = false,
                currentPage = 1,
                searchWallpapers = emptyList()
            )
        }
        performSearch(query, 1)
    }

    fun onClearSearch() = intent {
        currentActiveQuery = ""
        reduce {
            state.copy(
                isSearchMode = false,
                isEndReached = false,
                currentPage = 1
            )
        }
        if (state.wallpapers.isEmpty()) {
            loadWallpapers(1)
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) {
        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(wallpaper)
            } catch (_: Exception) {
                intent { postSideEffect(WallpaperListSideEffect.ShowSnackbar("Failed to update favorite")) }
            }
        }
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavoritesWallpapers()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                intent {
                    reduce {
                        state.copy(
                            favoriteIds = favoriteIds,
                            wallpapers = state.wallpapers.map { it.copy(isFavorite = it.id in favoriteIds) },
                            searchWallpapers = state.searchWallpapers.map { it.copy(isFavorite = it.id in favoriteIds) }
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}