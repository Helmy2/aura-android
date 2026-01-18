package com.example.aura.feature.videos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.VideoRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination.VideoDetail
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class VideosViewModel(
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator,
) : ViewModel(), ContainerHost<VideosState, VideosSideEffect> {

    override val container = container<VideosState, VideosSideEffect>(VideosState())

    private var currentActiveQuery: String = ""

    init {
        loadPopularVideos(1)
        observeFavorites()
    }

    private fun loadPopularVideos(page: Int) = intent {
        viewModelScope.launch {
            try {
                val videos = videoRepository.getPopularVideos(page)

                reduce {
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
                reduce {
                    state.copy(
                        isLoading = false,
                        isPaginationLoading = false,
                        error = e.message ?: "Failed to load videos"
                    )
                }
            }
        }
    }

    private fun performSearch(query: String, page: Int) {
        currentActiveQuery = query

        viewModelScope.launch {
            try {
                val videos = videoRepository.searchVideos(query, page)

                intent {
                    reduce {
                        val newVideos = if (page == 1) videos else state.searchVideos + videos
                        state.copy(
                            searchVideos = newVideos,
                            isLoading = false,
                            isPaginationLoading = false,
                            currentPage = page,
                            isEndReached = videos.isEmpty()
                        )
                    }
                }
            } catch (_: Exception) {
                intent {
                    postSideEffect(VideosSideEffect.ShowSnackbar("Search failed"))
                    reduce { state.copy(isLoading = false, isPaginationLoading = false) }
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

    fun onLoadNextPage() = intent {
        if (state.isPaginationLoading || state.isEndReached) return@intent

        val nextPage = state.currentPage + 1
        reduce { state.copy(isPaginationLoading = true) }

        if (state.isSearchMode) {
            performSearch(currentActiveQuery, nextPage)
        } else {
            loadPopularVideos(nextPage)
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
                searchVideos = emptyList()
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

        if (state.popularVideos.isEmpty()) {
            reduce { state.copy(isLoading = true) }
            loadPopularVideos(1)
        }
    }

    fun onFavoriteClicked(video: Video) {
        viewModelScope.launch {
            try {
                favoritesRepository.toggleFavorite(video)
            } catch (_: Exception) {
                intent { postSideEffect(VideosSideEffect.ShowSnackbar("Failed to toggle favorite")) }
            }
        }
    }

    private fun observeFavorites() {
        favoritesRepository.observeFavoriteVideos()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                intent {
                    reduce {
                        state.copy(
                            popularVideos = state.popularVideos.map { it.copy(isFavorite = it.id in favoriteIds) },
                            searchVideos = state.searchVideos.map { it.copy(isFavorite = it.id in favoriteIds) }
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
}
