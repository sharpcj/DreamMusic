package com.sharpcj.dreammusic.feature.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharpcj.dreammusic.R

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.mipmap.skin_player_bg),
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x66000000), Color(0x22000000), Color(0x99000000)),
                    ),
                ),
        )
        Column(modifier = Modifier.fillMaxSize()) {
            PlayerTopBar(uiState = uiState, onBack = onBack)
            PlayerCenter(uiState = uiState, modifier = Modifier.weight(1f))
            PlayerProgress(uiState = uiState, onSeekTo = viewModel::seekTo)
            LegacyPlaybackControls(
                uiState = uiState,
                onPrevious = viewModel::skipToPrevious,
                onPlayPause = viewModel::playOrPause,
                onNext = viewModel::skipToNext,
            )
            PlayerBottomActions(
                uiState = uiState,
                onStop = viewModel::stop,
                onRefresh = viewModel::refresh,
                onToggleFavorite = viewModel::toggleFavorite,
                onCyclePlaybackMode = viewModel::cyclePlaybackMode,
            )
            QueuePanel(uiState = uiState, onQueueItemClick = viewModel::playQueueItem)
        }
    }
}

@Composable
private fun PlayerTopBar(uiState: PlayerUiState, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 12.dp, start = 10.dp, end = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .size(30.dp)
                .clickable(onClick = onBack),
            painter = painterResource(R.mipmap.cloud_local_sheel_icon),
            contentDescription = "返回",
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = uiState.title.ifBlank { "DreamMusic" },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = uiState.artist.ifBlank { "未知艺术家" },
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlayerCenter(uiState: PlayerUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(238.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.30f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "♪",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (uiState.isPlaying) "正在播放" else "轻触播放键开始播放",
            color = Color.White.copy(alpha = 0.86f),
            style = MaterialTheme.typography.bodyMedium,
        )
        uiState.errorMessage?.let { message ->
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = message,
                color = Color(0xFFFFD8D8),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LegacyPlaybackControls(
    uiState: PlayerUiState,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier
                .size(60.dp)
                .clickable(enabled = uiState.isControllerReady && uiState.canSkipToPrevious, onClick = onPrevious),
            painter = painterResource(R.mipmap.lastsong),
            contentDescription = "上一首",
        )
        Image(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .size(70.dp)
                .clickable(enabled = uiState.isControllerReady, onClick = onPlayPause),
            painter = painterResource(R.mipmap.cz6),
            contentDescription = if (uiState.isPlaying) "暂停" else "播放",
        )
        Image(
            modifier = Modifier
                .size(60.dp)
                .clickable(enabled = uiState.isControllerReady && uiState.canSkipToNext, onClick = onNext),
            painter = painterResource(R.mipmap.nextsong),
            contentDescription = "下一首",
        )
    }
}

@Composable
private fun PlayerBottomActions(
    uiState: PlayerUiState,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCyclePlaybackMode: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerPill(text = uiState.playbackMode.label, enabled = uiState.isControllerReady, onClick = onCyclePlaybackMode)
        PlayerPill(text = if (uiState.isFavorite) "已喜欢" else "喜欢", enabled = uiState.currentSongId != null, onClick = onToggleFavorite)
        PlayerPill(text = "刷新", enabled = true, onClick = onRefresh)
        PlayerPill(text = "停止", enabled = uiState.isControllerReady, onClick = onStop)
    }
}

@Composable
private fun PlayerPill(text: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        color = Color.White.copy(alpha = if (enabled) 0.18f else 0.08f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            text = text,
            color = Color.White.copy(alpha = if (enabled) 0.95f else 0.45f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun QueuePanel(uiState: PlayerUiState, onQueueItemClick: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(150.dp),
        color = Color.Black.copy(alpha = 0.22f),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "播放队列", color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (uiState.queue.isEmpty()) "0 首" else "${uiState.currentQueueIndex + 1}/${uiState.queue.size}",
                    color = Color.White.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (uiState.queue.isEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = "暂无播放队列。请先从“本地音乐”点击一首歌曲。",
                    color = Color.White.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                LazyColumn(modifier = Modifier.padding(top = 6.dp)) {
                    itemsIndexed(
                        items = uiState.queue,
                        key = { index, item -> item.mediaId.ifBlank { "queue-$index" } },
                    ) { index, item ->
                        QueueItemRow(index = index, item = item, onClick = { onQueueItemClick(index) })
                        HorizontalDivider(color = Color.White.copy(alpha = 0.10f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueItemRow(
    index: Int,
    item: PlayerQueueItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (item.isCurrent) "▶" else "${index + 1}",
            color = if (item.isCurrent) Color.White else Color.White.copy(alpha = 0.70f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.artist.ifBlank { "未知艺术家" },
                color = Color.White.copy(alpha = 0.68f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlayerProgress(
    uiState: PlayerUiState,
    onSeekTo: (Long) -> Unit,
) {
    val canSeek = uiState.isControllerReady && uiState.durationMillis > 0L
    var isDragging by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(uiState.currentPositionMillis.toFloat()) }

    LaunchedEffect(uiState.currentPositionMillis, uiState.durationMillis) {
        if (!isDragging) {
            sliderPosition = uiState.currentPositionMillis.toFloat()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val visiblePosition = if (isDragging) sliderPosition.toLong() else uiState.currentPositionMillis
            Text(
                modifier = Modifier.width(50.dp),
                text = formatDuration(visiblePosition),
                color = Color.White,
                fontSize = 15.sp,
            )
            if (canSeek) {
                Slider(
                    value = sliderPosition.coerceIn(0f, uiState.durationMillis.toFloat()),
                    onValueChange = { value ->
                        isDragging = true
                        sliderPosition = value
                    },
                    onValueChangeFinished = {
                        isDragging = false
                        onSeekTo(sliderPosition.toLong())
                    },
                    valueRange = 0f..uiState.durationMillis.toFloat(),
                    modifier = Modifier.weight(1f),
                )
            } else {
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                modifier = Modifier.width(50.dp),
                text = formatDuration(uiState.durationMillis),
                color = Color.White,
                fontSize = 15.sp,
            )
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
