package com.example.aura.feature.wallpaper.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aura.R
import com.example.aura.domain.model.Wallpaper
import com.example.aura.shared.component.AuraImage
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.component.FavoriteButton
import com.example.aura.shared.component.SystemBarStyle
import com.example.aura.shared.core.extensions.toColor
import com.example.aura.shared.core.mvi.CollectSideEffect
import com.example.aura.shared.core.mvi.collectAsState
import com.example.aura.shared.theme.dimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WallpaperScreen(
    wallpaper: Wallpaper,
    viewModel: WallpaperViewModel = koinViewModel()
) {
    SystemBarStyle(isStatusBarOnDark = true, restoreOnDispose = true)

    val snackbarState = remember { SnackbarHostState() }
    val state by viewModel.collectAsState()

    LaunchedEffect(wallpaper) {
        viewModel.loadWallpaper(wallpaper)
    }

    viewModel.CollectSideEffect { effect ->
        when (effect) {
            is WallpaperDetailEffect.ShowMessage -> {
                snackbarState.showSnackbar(effect.message)
            }
            is WallpaperDetailEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true
                )
            }
        }
    }

    AuraScaffold(
        snackbarHost = {
            SnackbarHost(
                snackbarState,
                snackbar = {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                color = state.wallpaper?.averageColor?.toColor()
                                    ?: Color.Transparent
                            )
                    ) {
                        Text(
                            it.visuals.message,
                            color = Color.White,
                            modifier = Modifier.padding(MaterialTheme.dimens.md)
                        )
                    }
                }
            )
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.dimens.topBarScrimHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                AuraTransparentTopBar(
                    contentColor = Color.White,
                    title = stringResource(R.string.details),
                    onBackClick = viewModel::onBackClicked,
                    actions = {
                        state.wallpaper?.let { currentWallpaper ->
                            FavoriteButton(
                                isFavorite = currentWallpaper.isFavorite,
                                onClick = { viewModel.onToggleFavorite(currentWallpaper) },
                                tint = Color.White
                            )

                            IconButton(
                                onClick = viewModel::onDownloadClicked,
                                enabled = !state.isDownloading
                            ) {
                                if (state.isDownloading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = stringResource(R.string.download),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = state.wallpaper?.averageColor?.toColor() ?: Color.Transparent)
        ) {
            AuraImage(
                imageUrl = state.wallpaper?.imageUrl,
                contentDescription = state.wallpaper?.photographer,
                contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.Center)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.dimens.bottomOverlayHeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            state.wallpaper?.let { wallpaperData ->
                AnimatedVisibility(
                    visible = true,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(MaterialTheme.dimens.screenPadding)
                        .padding(bottom = padding.calculateBottomPadding())
                ) {
                    Text(
                        text = wallpaperData.photographer,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}