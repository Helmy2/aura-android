package com.example.aura.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.aura.R
import com.example.aura.domain.model.Video
import com.example.aura.shared.core.extensions.plus
import com.example.aura.shared.core.extensions.shimmerEffect
import com.example.aura.shared.theme.dimens
import java.util.Locale

@Composable
fun VideoGallery(
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onFavoriteClick: (Video) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    isLoading: Boolean = false,
    emptyContent: (@Composable () -> Unit)? = null,
    searchAppBar: (@Composable () -> Unit)? = null,
    isPaginationLoading: Boolean = false
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = adaptiveInfo.windowSizeClass

    val minSize =
        if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
            250.dp
        } else if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)) {
            200.dp
        } else {
            150.dp
        }

    if (videos.isEmpty() && !isLoading && !isPaginationLoading) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(
                    contentPadding + PaddingValues(MaterialTheme.dimens.md)
                ),
        ) {
            if (searchAppBar != null)
                searchAppBar()
            if (emptyContent != null)
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    emptyContent()
                }
        }
    } else {
        LazyVerticalStaggeredGrid(
            state = listState,
            columns = StaggeredGridCells.Adaptive(minSize = minSize),
            contentPadding = contentPadding + PaddingValues(MaterialTheme.dimens.md),
            verticalItemSpacing = MaterialTheme.dimens.md,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.md),
            modifier = modifier
        ) {
            if (searchAppBar != null)
                item(span = StaggeredGridItemSpan.FullLine) {
                    searchAppBar()
                }
            if (emptyContent != null && videos.isEmpty())
                item(span = StaggeredGridItemSpan.FullLine) {
                    emptyContent()
                }
            if (!isLoading)
                items(videos, key = { it.id }) { video ->
                    VideoItem(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onFavoriteClick = { onFavoriteClick(video) },
                        modifier = Modifier.animateItem()
                    )
                }
            if (isPaginationLoading || isLoading) {
                items(10) {
                    Card(
                        modifier = Modifier
                            .padding(MaterialTheme.dimens.xs)
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shimmerEffect(),
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun VideoItem(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatDuration(video.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.play),
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(8.dp)
                )
            }

            FavoriteButton(
                isFavorite = video.isFavorite,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.ENGLISH, "%d:%02d", m, s)
}
