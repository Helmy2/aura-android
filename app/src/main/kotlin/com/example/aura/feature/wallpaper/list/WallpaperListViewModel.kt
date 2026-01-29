package com.example.aura.feature.wallpaper.list

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Wallpaper
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.WallpaperRepository
import com.example.aura.shared.core.mvi.ContainerHost
import com.example.aura.shared.core.mvi.ContainerSettings
import com.example.aura.shared.core.mvi.container
import com.example.aura.shared.core.mvi.intent
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class WallpaperListViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : ContainerHost<WallpaperListState, WallpaperListEffect>, ViewModel() {

    override val container = container<WallpaperListState, WallpaperListEffect>(
        initialState = WallpaperListState(),
        settings = ContainerSettings(
            exceptionHandler = { throwable ->
                Log.e(TAG, "Unhandled error: $throwable")
                intent {
                    postSideEffect(WallpaperListEffect.ShowError("An unexpected error occurred"))
                }
            }
        )
    )

    init {
        loadWallpapers(page = 1)
        observeFavorites()
    }

    private fun loadWallpapers(page: Int) = intent {
        try {
            val newWallpapers = wallpaperRepository.getCuratedWallpapers(page = page)

            reduce {
                val combinedWallpapers = if (page == 1) newWallpapers
                else state.wallpapers + newWallpapers
                state.copy(
                    wallpapers = combinedWallpapers,
                    isLoading = false,
                    isPaginationLoading = false,
                    currentPage = page,
                    isEndReached = newWallpapers.isEmpty(),
                    error = null
                )
            }
        } catch (e: Exception) {
            reduce {
                copy(isLoading = false, isPaginationLoading = false, error = e.message)
            }
            postSideEffect(
                WallpaperListEffect.ShowError(e.message ?: "Failed to load wallpapers")
            )
        }
    }

    private fun performSearch(query: String, page: Int) = intent {
        reduce { copy(searchQuery = query) }

        try {
            val results = wallpaperRepository.searchWallpapers(query, page)

            reduce {
                val combinedResults = if (page == 1) results
                else state.searchWallpapers + results
                state.copy(
                    searchWallpapers = combinedResults,
                    isLoading = false,
                    isPaginationLoading = false,
                    currentPage = page,
                    isEndReached = results.isEmpty()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed for query: $query", e)
            reduce {
                copy(isLoading = false, isPaginationLoading = false)
            }
            postSideEffect(WallpaperListEffect.ShowError("Search failed"))
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
        reduce { copy(isPaginationLoading = true) }

        if (state.isSearchMode) {
            performSearch(state.searchQuery, nextPage)
        } else {
            loadWallpapers(nextPage)
        }
    }

    fun onSearchTriggered(query: String) = intent {
        if (query.isBlank()) return@intent

        reduce {
            copy(
                isSearchMode = true,
                isLoading = true,
                isEndReached = false,
                currentPage = 1,
                searchQuery = query,
                searchWallpapers = emptyList()
            )
        }

        performSearch(query, 1)
    }

    fun onClearSearch() = intent {
        reduce {
            copy(
                isSearchMode = false,
                isEndReached = false,
                currentPage = 1,
                searchQuery = ""
            )
        }

        if (state.wallpapers.isEmpty()) {
            loadWallpapers(1)
        }
    }

    fun onToggleFavorite(wallpaper: Wallpaper) = intent {
        try {
            favoritesRepository.toggleFavorite(wallpaper)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite for wallpaper: ${wallpaper.id}", e)
            postSideEffect(WallpaperListEffect.ShowError("Failed to update favorite"))
        }
    }

    private fun observeFavorites() = intent {
        favoritesRepository.observeFavoritesWallpapers()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                reduce {
                    state.copy(
                        favoriteIds = favoriteIds,
                        wallpapers = state.wallpapers.map {
                            it.copy(isFavorite = it.id in favoriteIds)
                        },
                        searchWallpapers = state.searchWallpapers.map {
                            it.copy(isFavorite = it.id in favoriteIds)
                        }
                    )
                }
            }
            .collect()
    }

    companion object {
        private const val TAG = "WallpaperListViewModel"
    }
}