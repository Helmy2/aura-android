package com.example.aura.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aura.R
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.MediaContentGallery
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.example.aura.shared.theme.dimens
import org.koin.compose.viewmodel.koinViewModel

@Suppress("ParamsComparedByRef")
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onCreate()
    }

    viewModel.collectSideEffect {
        when (it) {
            is FavoritesEffect.ShowUserMessage -> {
                snackbarHostState.showSnackbar(it.message)
            }
        }
    }

    AuraScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box {
            if (state.error != null) {
                Text(
                    text = stringResource(id = R.string.search_failed) + ": ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                MediaContentGallery(
                    contentPadding = padding,
                    items = state.items,
                    onItemClick = viewModel::onItemClicked,
                    onFavoriteClick = viewModel::onRemoveFavorite,
                    isLoading = state.isLoading,
                    emptyContent = { EmptyFavoritesView() }
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesView(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.no_favorites)
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.md)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = stringResource(R.string.start_adding_favorites),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.dimens.xl)
            )
        }
    }
}
