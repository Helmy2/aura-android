package com.example.aura.feature.videos.list

import androidx.lifecycle.ViewModel
import com.example.aura.domain.model.Video
import com.example.aura.domain.repository.FavoritesRepository
import com.example.aura.domain.repository.VideoRepository
import com.example.aura.shared.navigation.AppNavigator
import com.example.aura.shared.navigation.Destination.VideoDetail
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container

class VideosViewModel(
    private val videoRepository: VideoRepository,
    private val favoritesRepository: FavoritesRepository,
    private val navigator: AppNavigator
) : ContainerHost<VideosState, VideosEffect>, ViewModel() {

    override val container = container<VideosState, VideosEffect>(VideosState())

    fun onCreate() = intent {
        loadPopularVideos(1)
        observeFavorites()
    }

    private fun loadPopularVideos(page: Int) = intent {
        try {
            if (page == 1) {
                reduce { state.copy(isLoading = true) }
            }

            val videos = videoRepository.getPopularVideos(page)

            reduce {
                val newVideos = if (page == 1) videos else state.popularVideos + videos
                state.copy(
                    popularVideos = newVideos,
                    isLoading = false,
                    isPaginationLoading = false,
                    currentPage = page,
                    isEndReached = videos.isEmpty(),
                    error = null
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
            postSideEffect(
                VideosEffect.ShowError(e.message ?: "Failed to load videos")
            )
        }
    }

    private fun performSearch(query: String, page: Int) = intent {
        reduce { state.copy(searchQuery = query) }

        try {
            val videos = videoRepository.searchVideos(query, page)

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
        } catch (_: Exception) {
            reduce {
                state.copy(isLoading = false, isPaginationLoading = false)
            }
            postSideEffect(VideosEffect.ShowError("Search failed"))
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
            performSearch(state.searchQuery, nextPage)
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
                searchQuery = query,
                searchVideos = emptyList()
            )
        }

        performSearch(query, 1)
    }

    fun onClearSearch() = intent {
        reduce {
            state.copy(
                isSearchMode = false,
                isEndReached = false,
                currentPage = 1,
                searchQuery = ""
            )
        }

        if (state.popularVideos.isEmpty()) {
            reduce { state.copy(isLoading = true) }
            loadPopularVideos(1)
        }
    }

    fun onFavoriteClicked(video: Video) = intent {
        try {
            favoritesRepository.toggleFavorite(video)
        } catch (_: Exception) {
            postSideEffect(VideosEffect.ShowError("Failed to toggle favorite"))
        }
    }

    private fun observeFavorites() = intent {
        favoritesRepository.observeFavoriteVideos()
            .map { favorites -> favorites.map { it.id }.toSet() }
            .onEach { favoriteIds ->
                reduce {
                    state.copy(
                        popularVideos = state.popularVideos.map {
                            it.copy(isFavorite = it.id in favoriteIds)
                        },
                        searchVideos = state.searchVideos.map {
                            it.copy(isFavorite = it.id in favoriteIds)
                        }
                    )
                }
            }
            .collect()
    }
}