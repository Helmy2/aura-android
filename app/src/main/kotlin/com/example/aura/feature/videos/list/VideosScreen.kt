package com.example.aura.feature.videos.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.AuraSearchBar
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.component.VideoGallery
import org.koin.compose.viewmodel.koinViewModel

@Suppress("ParamsComparedByRef")
@Composable
fun VideosScreen(
    viewModel: VideosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyStaggeredGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    val searchState = rememberTextFieldState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            totalItems > 0 &&
                    lastVisibleIndex >= (totalItems - 4) &&
                    !state.isLoading &&
                    !state.isPaginationLoading &&
                    !state.isEndReached
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.onLoadNextPage()
        }
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    AuraScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuraTransparentTopBar(
                title = "Videos",
                onBackClick = viewModel::onBackClicked
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.error != null && state.popularVideos.isEmpty()) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                VideoGallery(
                    contentPadding = padding,
                    videos = if (state.isSearchMode) state.searchVideos else state.popularVideos,
                    onVideoClick = viewModel::onVideoClicked,
                    onFavoriteClick = viewModel::onFavoriteClicked,
                    isLoading = state.isLoading,
                    isPaginationLoading = state.isPaginationLoading,
                    searchAppBar = {
                        AuraSearchBar(
                            state = searchState,
                            onSearch = viewModel::onSearchTriggered,
                            onClearSearch = viewModel::onClearSearch
                        )
                    },
                    emptyContent = {
                        Text(text = "No results found")
                    }
                )
            }
        }
    }
}