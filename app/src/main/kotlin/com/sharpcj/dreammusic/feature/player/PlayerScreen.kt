package com.sharpcj.dreammusic.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Text(text = "播放器", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            NowPlaying(uiState = uiState)
        }

        item {
            PlayerProgress(uiState = uiState)
        }

        uiState.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            PlaybackControls(
                uiState = uiState,
                onPrevious = viewModel::skipToPrevious,
                onPlayPause = viewModel::playOrPause,
                onNext = viewModel::skipToNext,
                onStop = viewModel::stop,
                onRefresh = viewModel::refresh,
            )
        }

        item {
            PlaybackModeRow(
                uiState = uiState,
                onCyclePlaybackMode = viewModel::cyclePlaybackMode,
            )
        }

        item {
            QueueHeader(uiState = uiState)
        }

        if (uiState.queue.isEmpty()) {
            item {
                Text(
                    text = "暂无播放队列。请先从“本地音乐”点击一首歌曲。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            itemsIndexed(
                items = uiState.queue,
                key = { index, item -> item.mediaId.ifBlank { "queue-$index" } },
            ) { index, item ->
                QueueItemRow(
                    index = index,
                    item = item,
                    onClick = { viewModel.playQueueItem(index) },
                )
                HorizontalDivider()
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier.padding(top = 4.dp),
                onClick = onBack,
            ) {
                Text("返回")
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun NowPlaying(uiState: PlayerUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = uiState.artist.ifBlank { "未知艺术家" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PlaybackControls(
    uiState: PlayerUiState,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onStop: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = uiState.isControllerReady && uiState.canSkipToPrevious,
        ) {
            Text("上一首")
        }
        Button(
            onClick = onPlayPause,
            enabled = uiState.isControllerReady,
        ) {
            Text(if (uiState.isPlaying) "暂停" else "播放")
        }
        OutlinedButton(
            onClick = onNext,
            enabled = uiState.isControllerReady && uiState.canSkipToNext,
        ) {
            Text("下一首")
        }
    }

    Row(
        modifier = Modifier.padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onStop,
            enabled = uiState.isControllerReady,
        ) {
            Text("停止")
        }
        OutlinedButton(onClick = onRefresh) { Text("刷新") }
    }
}

@Composable
private fun PlaybackModeRow(
    uiState: PlayerUiState,
    onCyclePlaybackMode: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = "播放模式", style = MaterialTheme.typography.titleMedium)
            Text(
                text = uiState.playbackMode.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = onCyclePlaybackMode,
            enabled = uiState.isControllerReady,
        ) {
            Text("切换模式")
        }
    }
}

@Composable
private fun QueueHeader(uiState: PlayerUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "播放队列", style = MaterialTheme.typography.titleMedium)
        Text(
            text = if (uiState.queue.isEmpty()) {
                "0 首"
            } else {
                "${uiState.currentQueueIndex + 1}/${uiState.queue.size}"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QueueItemRow(
    index: Int,
    item: PlayerQueueItem,
    onClick: () -> Unit,
) {
    val containerColor = if (item.isCurrent) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (item.isCurrent) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (item.isCurrent) "▶" else "${index + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.artist.ifBlank { "未知艺术家" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isCurrent) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerProgress(uiState: PlayerUiState) {
    val progress = if (uiState.durationMillis > 0L) {
        (uiState.currentPositionMillis.toFloat() / uiState.durationMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(uiState.currentPositionMillis), style = MaterialTheme.typography.bodySmall)
            Text(formatDuration(uiState.durationMillis), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
