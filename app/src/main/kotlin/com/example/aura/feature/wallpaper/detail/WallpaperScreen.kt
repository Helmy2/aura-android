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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aura.domain.model.Wallpaper
import com.example.aura.shared.component.AuraImage
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.component.AuraTransparentTopBar
import com.example.aura.shared.component.FavoriteButton
import com.example.aura.shared.component.SystemBarStyle
import com.example.aura.shared.core.extensions.toColor
import com.example.aura.shared.theme.dimens
import org.koin.compose.viewmodel.koinViewModel

@Suppress("ParamsComparedByRef")
@Composable
fun WallpaperScreen(
    wallpaper: Wallpaper,
    viewModel: WallpaperViewModel = koinViewModel()
) {
    SystemBarStyle(isStatusBarOnDark = true, restoreOnDispose = true)

    val snackbarState = remember { SnackbarHostState() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(wallpaper) {
        viewModel.loadWallpaper(wallpaper)
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarState.showSnackbar(message)
            viewModel.onMessageShown()
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
                            modifier = Modifier.padding(MaterialTheme.dimens.md),
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
                    title = "Details",
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
                                        contentDescription = "Download",
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
                contentDescription = null,
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
                                Color.Transparent, Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            AnimatedVisibility(
                state.wallpaper != null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(MaterialTheme.dimens.screenPadding)
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                Text(
                    text = state.wallpaper!!.photographer,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }
}