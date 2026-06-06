package com.sharpcj.dreammusic.feature.recent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sharpcj.dreammusic.core.model.RecentPlayedSong
import java.text.DateFormat
import java.util.Date

@Composable
fun RecentPlaysScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: RecentPlaysViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) { Text("返回") }
            Button(onClick = onOpenPlayer) { Text("播放器") }
        }

        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = "最近播放",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "按最近一次播放时间倒序展示。点击歌曲会按最近播放列表作为队列播放。",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = uiState.items.isNotEmpty(),
                onClick = viewModel::playAll,
            ) {
                Text("播放最近")
            }
            OutlinedButton(
                enabled = uiState.items.isNotEmpty(),
                onClick = viewModel::clearHistory,
            ) {
                Text("清空记录")
            }
        }

        if (uiState.items.isEmpty()) {
            EmptyRecentPlaysMessage()
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(items = uiState.items, key = { it.song.id }) { item ->
                    RecentPlayedSongRow(
                        item = item,
                        onClick = { viewModel.play(item.song) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun RecentPlayedSongRow(item: RecentPlayedSong, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = item.song.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = "${item.song.artist} · ${item.song.album}",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = "${formatDuration(item.song.durationMillis)} · ${formatPlayedAt(item.playedAtMillis)}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun EmptyRecentPlaysMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "还没有最近播放记录", style = MaterialTheme.typography.bodyLarge)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "从音乐库、搜索结果、艺术家或专辑详情播放歌曲后，这里会自动出现记录。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun formatPlayedAt(playedAtMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(playedAtMillis))
