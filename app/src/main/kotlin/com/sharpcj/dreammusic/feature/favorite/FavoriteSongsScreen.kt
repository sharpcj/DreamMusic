package com.sharpcj.dreammusic.feature.favorite

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
import com.sharpcj.dreammusic.core.model.FavoriteSong
import java.text.DateFormat
import java.util.Date

private val FavoriteGreen = Color(0xFF31C27C)
private val FavoriteBackground = Color(0xFFF4F4F4)
private val FavoriteSecondary = Color(0xFF8E8E93)

@Composable
fun FavoriteSongsScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: FavoriteSongsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FavoriteBackground),
    ) {
        FavoriteTopBar(onBack = onBack, onOpenPlayer = onOpenPlayer)
        FavoriteActionHeader(
            count = uiState.items.size,
            enabled = uiState.items.isNotEmpty(),
            onPlayAll = viewModel::playAll,
            onClear = viewModel::clearFavorites,
        )
        if (uiState.items.isEmpty()) {
            EmptyFavoriteSongsMessage()
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = uiState.items, key = { it.song.id }) { item ->
                    FavoriteSongRow(
                        item = item,
                        onClick = { viewModel.play(item.song) },
                        onRemove = { viewModel.removeFavorite(item.song) },
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun FavoriteTopBar(onBack: () -> Unit, onOpenPlayer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FavoriteGreen),
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
            text = "我喜欢",
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
private fun FavoriteActionHeader(count: Int, enabled: Boolean, onPlayAll: () -> Unit, onClear: () -> Unit) {
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
                Text(text = "全部歌曲", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "$count 首 · 按收藏时间倒序展示",
                    color = FavoriteSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(enabled = enabled, onClick = onPlayAll) { Text("播放喜欢") }
            Spacer(modifier = Modifier.size(8.dp))
            OutlinedButton(enabled = enabled, onClick = onClear) { Text("清空喜欢") }
        }
    }
}

@Composable
private fun FavoriteSongRow(item: FavoriteSong, onClick: () -> Unit, onRemove: () -> Unit) {
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
                        color = FavoriteSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = "${formatDuration(item.song.durationMillis)} · 收藏于 ${formatTime(item.favoritedAtMillis)}",
                    color = FavoriteSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Surface(
                modifier = Modifier.padding(horizontal = 8.dp).clickable(onClick = onRemove),
                shape = RoundedCornerShape(999.dp),
                color = FavoriteGreen.copy(alpha = 0.12f),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    text = "移除",
                    color = FavoriteGreen,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Image(modifier = Modifier.size(18.dp), painter = painterResource(R.mipmap.more_version_arrow), contentDescription = null)
        }
    }
}

@Composable
private fun EmptyFavoriteSongsMessage() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "还没有喜欢的歌曲", style = MaterialTheme.typography.bodyLarge)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "可以在音乐库、搜索结果、最近播放里点击“喜欢”加入收藏。",
            color = FavoriteSecondary,
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

private fun formatTime(timeMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timeMillis))
