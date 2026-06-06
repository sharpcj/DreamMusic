package com.sharpcj.dreammusic.feature.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
fun LibraryScreen(
    onOpenPlayer: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.refreshLocalSongs()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.hasMediaPermission()) {
            viewModel.refreshLocalSongs()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(text = "本地音乐", style = MaterialTheme.typography.headlineMedium)
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "已从旧 LocalMusicUtils 迁移为 MediaStore + Room + Repository + ViewModel。",
            style = MaterialTheme.typography.bodyMedium,
        )

        LibraryActions(
            uiState = uiState,
            onRefresh = {
                if (viewModel.hasMediaPermission()) {
                    viewModel.refreshLocalSongs()
                } else {
                    permissionLauncher.launch(viewModel.mediaPermission)
                }
            },
            onOpenPlayer = onOpenPlayer,
        )

        val errorMessage = uiState.errorMessage
        when {
            uiState.songs.isNotEmpty() -> SongList(
                songs = uiState.songs,
                onSongClick = viewModel::play,
            )
            errorMessage != null -> EmptyLibraryMessage(errorMessage)
            else -> EmptyLibraryMessage("点击“扫描本地音乐”读取设备上的音频文件。")
        }
    }
}

@Composable
private fun LibraryActions(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onRefresh, enabled = !uiState.isRefreshing) {
            Text(if (uiState.isRefreshing) "扫描中..." else "扫描本地音乐")
        }
        Button(onClick = onOpenPlayer) { Text("打开播放器") }
    }
    uiState.lastRefreshCount?.let { count ->
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "最近一次扫描到 $count 首歌曲",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SongList(
    songs: List<LocalSong>,
    onSongClick: (LocalSong) -> Unit,
) {
    LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
        items(items = songs, key = { it.id }) { song ->
            SongRow(song = song, onClick = { onSongClick(song) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun SongRow(song: LocalSong, onClick: () -> Unit) {
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
            text = formatDuration(song.durationMillis),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun EmptyLibraryMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "当前会申请 ${Manifest.permission.READ_MEDIA_AUDIO} 权限。",
                style = MaterialTheme.typography.bodySmall,
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
