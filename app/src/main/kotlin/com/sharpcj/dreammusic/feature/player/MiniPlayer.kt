package com.sharpcj.dreammusic.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MiniPlayer(
    uiState: PlayerUiState,
    onSkipToPrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipToNext: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.hasCurrentMedia) return

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onOpenPlayer),
    ) {
        Column {
            LinearProgressIndicator(
                progress = { uiState.playbackProgress },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = uiState.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = uiState.artist.ifBlank { "未知艺术家" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Button(
                    onClick = onSkipToPrevious,
                    enabled = uiState.isControllerReady && uiState.canSkipToPrevious,
                ) {
                    Text("上")
                }
                Button(
                    onClick = onTogglePlayPause,
                    enabled = uiState.isControllerReady,
                ) {
                    Text(if (uiState.isPlaying) "暂停" else "播放")
                }
                Button(
                    onClick = onSkipToNext,
                    enabled = uiState.isControllerReady && uiState.canSkipToNext,
                ) {
                    Text("下")
                }
            }
        }
    }
}

private val PlayerUiState.playbackProgress: Float
    get() = if (durationMillis > 0L) {
        (currentPositionMillis.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
