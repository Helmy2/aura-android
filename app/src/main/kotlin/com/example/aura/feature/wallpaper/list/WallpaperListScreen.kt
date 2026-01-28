package com.example.aura.feature.wallpaper.list

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
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.AuraSearchBar
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.component.WallpaperGallery
import com.example.aura.shared.core.mvi.CollectSideEffect
import com.example.aura.shared.core.mvi.collectAsState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WallpaperListScreen(
    viewModel: WallpaperListViewModel = koinViewModel()
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
                    lastVisibleIndex >= (totalItems - 5) &&
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

    viewModel.CollectSideEffect { effect ->
        when (effect) {
            is WallpaperListEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true
                )
            }
            is WallpaperListEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    AuraScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AuraTransparentTopBar(
                title = "Wallpapers",
                onBackClick = viewModel::onBackClicked
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.error != null && state.wallpapers.isEmpty()) {
                Text(
                    text = "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                WallpaperGallery(
                    contentPadding = padding,
                    listState = listState,
                    wallpapers = if (state.isSearchMode) state.searchWallpapers
                    else state.wallpapers,
                    onWallpaperClick = viewModel::onWallpaperClicked,
                    onWallpaperFavoriteClick = viewModel::onToggleFavorite,
                    isPaginationLoading = state.isPaginationLoading,
                    isLoading = state.isLoading,
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