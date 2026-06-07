package com.sharpcj.dreammusic.feature.recent

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharpcj.dreammusic.R
import com.sharpcj.dreammusic.core.model.RecentPlayedSong
import java.text.DateFormat
import java.util.Date

private val RecentGreen = Color(0xFF31C27C)
private val RecentBackground = Color(0xFFF4F4F4)
private val RecentSecondary = Color(0xFF8E8E93)

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
            .background(RecentBackground),
    ) {
        ListTopBar(title = "最近播放", onBack = onBack, onOpenPlayer = onOpenPlayer)
        ListActionHeader(
            title = "全部歌曲",
            subtitle = "${uiState.items.size} 首 · 按最近一次播放时间倒序展示",
            primary = "播放最近",
            secondary = "清空记录",
            enabled = uiState.items.isNotEmpty(),
            onPrimary = viewModel::playAll,
            onSecondary = viewModel::clearHistory,
        )
        if (uiState.items.isEmpty()) {
            EmptyRecentPlaysMessage()
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = uiState.items, key = { it.song.id }) { item ->
                    RecentPlayedSongRow(
                        item = item,
                        isFavorite = item.song.id in uiState.favoriteSongIds,
                        onClick = { viewModel.play(item.song) },
                        onToggleFavorite = { viewModel.toggleFavorite(item.song) },
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun ListTopBar(title: String, onBack: () -> Unit, onOpenPlayer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(RecentGreen),
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(28.dp)
                .clickable(onClick = onBack),
            painter = painterResource(R.mipmap.back),
            contentDescription = "返回",
        )
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .clickable(onClick = onOpenPlayer),
            text = "播放器",
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ListActionHeader(
    title: String,
    subtitle: String,
    primary: String,
    secondary: String,
    enabled: Boolean,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(modifier = Modifier.padding(top = 4.dp), text = subtitle, color = RecentSecondary, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(enabled = enabled, onClick = onPrimary) { Text(primary) }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedButton(enabled = enabled, onClick = onSecondary) { Text(secondary) }
        }
    }
}

@Composable
private fun RecentPlayedSongRow(
    item: RecentPlayedSong,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.song.title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(modifier = Modifier.size(14.dp), painter = painterResource(R.mipmap.known_artist_icon), contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = "${item.song.artist} · ${item.song.album}",
                        color = RecentSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = "${formatDuration(item.song.durationMillis)} · ${formatPlayedAt(item.playedAtMillis)}",
                    color = RecentSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Surface(
                modifier = Modifier.padding(horizontal = 8.dp).clickable(onClick = onToggleFavorite),
                shape = RoundedCornerShape(999.dp),
                color = if (isFavorite) RecentGreen.copy(alpha = 0.12f) else RecentBackground,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    text = if (isFavorite) "已喜欢" else "喜欢",
                    color = if (isFavorite) RecentGreen else RecentSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Image(modifier = Modifier.size(18.dp), painter = painterResource(R.mipmap.more_version_arrow), contentDescription = null)
        }
    }
}

@Composable
private fun EmptyRecentPlaysMessage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "还没有最近播放记录", style = MaterialTheme.typography.bodyLarge)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "从音乐库、搜索结果、艺术家或专辑详情播放歌曲后，这里会自动出现记录。",
            color = RecentSecondary,
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
