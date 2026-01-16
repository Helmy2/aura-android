package com.example.aura.feature.videos.list

import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.VideoRepository
import com.example.aura.shared.core.util.StateViewModel
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination.VideoDetail
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class VideosViewModel(
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
) : StateViewModel<VideosState>(VideosState()) {

    init {
        loadPopularVideos(1)
        observeFavorites()
    }

    private fun loadPopularVideos(page: Int) {
        viewModelScope.launch {
            try {
                val videos = videoRepository.getPopularVideos(page)

                updateState { state ->
                    val newVideos = if (page == 1) videos else state.popularVideos + videos
                    state.copy(
                        popularVideos = newVideos,
                        isLoading = false,
                        isPaginationLoading = false,
                        currentPage = page,
                        isEndReached = videos.isEmpty()
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        isPaginationLoading = false,
                        error = e.message ?: "Failed to load videos"
                    )
                }
            }
        }
    }

    private fun performSearch(query: String, page: Int) {
        viewModelScope.launch {
            try {
                val videos = videoRepository.searchVideos(query, page)

                updateState { state ->
                    val newVideos = if (page == 1) videos else state.searchVideos + videos
                    state.copy(
                        searchVideos = newVideos,
                        isLoading = false,
                        isPaginationLoading = false,
                        currentPage = page,
                        isEndReached = videos.isEmpty()
                    )
                }
            } catch (_: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        isPaginationLoading = false,
                        userMessage = "Search failed"
                    )
                }
            }
        }
    }

    fun onVideoClicked(video: Video) {
        navigator.navigate(VideoDetail(video))
    }

    fun onBackClicked() {
        navigator.back()
    }

    fun onLoadNextPage() {
        val state = currentState
        if (state.isPaginationLoading || state.isEndReached) return

        val nextPage = state.currentPage + 1
        updateState { it.copy(isPaginationLoading = true) }

        if (state.isSearchMode) {
            performSearch(state.searchQuery, nextPage)
        } else {
            loadPopularVideos(nextPage)
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
                searchVideos = emptyList()
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
        if (currentState.popularVideos.isEmpty()) {
            updateState { it.copy(isLoading = true) }
            loadPopularVideos(1)
        }
    }

    fun onFavoriteClicked(video: Video) {
        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(video)
            } catch (_: Exception) {
                updateState { it.copy(userMessage = "Failed to toggle favorite") }
            }
        }
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavoriteVideos()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                updateState { state ->
                    state.copy(
                        popularVideos = state.popularVideos.map { it.copy(isFavorite = it.id in favoriteIds) },
                        searchVideos = state.searchVideos.map { it.copy(isFavorite = it.id in favoriteIds) }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onMessageShown() {
        updateState { it.copy(userMessage = null) }
    }
}
