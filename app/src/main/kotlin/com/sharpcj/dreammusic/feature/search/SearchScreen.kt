package com.sharpcj.dreammusic.feature.search

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharpcj.dreammusic.core.model.LocalSong

@Composable
fun SearchScreen(
    onOpenPlayer: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(text = "搜索", style = MaterialTheme.typography.headlineMedium)
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "搜索本地音乐库里的歌曲名和艺术家，点击结果即可按当前搜索结果队列播放。",
            style = MaterialTheme.typography.bodyMedium,
        )

        SearchInput(
            query = uiState.query,
            onQueryChange = viewModel::updateQuery,
            onClear = viewModel::clearQuery,
        )

        SearchContent(
            uiState = uiState,
            onSongClick = viewModel::play,
            onOpenPlayer = onOpenPlayer,
        )
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            label = { Text("歌曲名 / 艺术家") },
            placeholder = { Text("例如：周杰伦、晴天") },
        )
        TextButton(onClick = onClear, enabled = query.isNotBlank()) {
            Text("清空")
        }
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onSongClick: (LocalSong) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    when {
        uiState.isLibraryEmpty -> SearchEmptyMessage(
            title = "本地音乐库还是空的",
            message = "先到“音乐库”扫描本地音乐，搜索页会复用同一份本地歌曲缓存。",
        )
        !uiState.hasQuery -> SearchEmptyMessage(
            title = "输入关键词开始搜索",
            message = "当前音乐库共有 ${uiState.allSongs.size} 首歌曲。支持按歌曲名和艺术家过滤。",
        )
        uiState.hasResults -> SearchResults(
            query = uiState.query.trim(),
            songs = uiState.results,
            onSongClick = onSongClick,
            onOpenPlayer = onOpenPlayer,
        )
        else -> SearchEmptyMessage(
            title = "没有匹配结果",
            message = "没有找到包含“${uiState.query.trim()}”的歌曲名或艺术家。",
        )
    }
}

@Composable
private fun SearchResults(
    query: String,
    songs: List<LocalSong>,
    onSongClick: (LocalSong) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "“$query” 找到 ${songs.size} 首",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Button(onClick = onOpenPlayer) { Text("播放器") }
        }

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(items = songs, key = { it.id }) { song ->
                SearchSongRow(song = song, onClick = { onSongClick(song) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SearchSongRow(song: LocalSong, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = "${song.artist} · ${song.album}",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = "点击后按当前搜索结果队列播放 · ${formatDuration(song.durationMillis)}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SearchEmptyMessage(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
