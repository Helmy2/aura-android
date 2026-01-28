package com.example.aura.feature.videos.detail

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.example.aura.domain.model.Video
import com.example.aura.shared.component.AuraScaffold
import com.example.aura.shared.core.mvi.CollectSideEffect
import com.example.aura.shared.core.mvi.collectAsState
import com.example.aura.shared.theme.dimens
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VideoDetailScreen(
    video: Video,
    viewModel: VideoDetailViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(video) {
        viewModel.loadVideo(video)
    }

    viewModel.CollectSideEffect { effect ->
        when (effect) {
            is VideoDetailEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.message)
            }
            is VideoDetailEffect.ShowError -> {
                snackbarHostState.showSnackbar(
                    message = effect.message,
                    withDismissAction = true
                )
            }
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }

    LaunchedEffect(state.video) {
        state.video?.let { v ->
            val mediaItem = MediaItem.fromUri(v.videoUrl)
            if (exoPlayer.currentMediaItem?.mediaId != v.videoUrl) {
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AuraScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it)
                    .padding(MaterialTheme.dimens.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = viewModel::onBackClicked,
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = viewModel::onToggleFavorite,
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
                ) {
                    val isFavorite = state.video?.isFavorite == true
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }

                IconButton(
                    onClick = viewModel::onDownloadClicked,
                    modifier = Modifier.background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
                ) {
                    if (state.isDownloading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.padding(MaterialTheme.dimens.sm),
                            strokeWidth = 2.dp
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (state.video != null) {
                val currentVideo = state.video!!
                val aspectRatio = if (currentVideo.height > 0)
                    currentVideo.width.toFloat() / currentVideo.height else 16f / 9f

                var areControlsVisible by remember { mutableStateOf(true) }

                LaunchedEffect(areControlsVisible, exoPlayer.isPlaying) {
                    if (areControlsVisible && exoPlayer.isPlaying) {
                        delay(3000)
                        areControlsVisible = false
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .padding(MaterialTheme.dimens.md)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Color.DarkGray)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { areControlsVisible = !areControlsVisible },
                    contentAlignment = Alignment.Center
                ) {
                    PlayerSurface(
                        player = exoPlayer,
                        modifier = Modifier.fillMaxSize()
                    )

                    AnimatedVisibility(
                        visible = areControlsVisible || !exoPlayer.isPlaying,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            val showButton = areControlsVisible || !exoPlayer.isPlaying

                            AnimatedVisibility(
                                visible = showButton,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                PlayPauseButton(player = exoPlayer)
                            }

                        }
                    }
                }
            } else if (state.isLoading) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayPauseButton(player: Player, modifier: Modifier = Modifier) {
    val state = rememberPlayPauseButtonState(player)
    val icon = if (state.showPlay) Icons.Default.PlayArrow else Icons.Default.Pause
    val backgroundColor = Color.Black.copy(alpha = 0.5f)

    IconButton(
        onClick = state::onClick,
        modifier = modifier
            .size(72.dp)
            .background(backgroundColor, CircleShape)
            .padding(12.dp),
        enabled = state.isEnabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (state.showPlay) "Play" else "Pause",
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}