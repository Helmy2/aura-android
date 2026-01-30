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
import androidx.compose.ui.res.stringResource
import com.example.aura.R
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.AuraSearchBar
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.component.VideoGallery
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VideosScreen(
    viewModel: VideosViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
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

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.onLoadNextPage()
        }
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is VideosEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true
                )
            }
            is VideosEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    AuraScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuraTransparentTopBar(
                title = stringResource(R.string.videos),
                onBackClick = viewModel::onBackClicked
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.error != null && state.popularVideos.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.search_failed) + ": ${state.error}",
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
                        Text(text = stringResource(R.string.no_results))
                    }
                )
            }
        }
    }
}