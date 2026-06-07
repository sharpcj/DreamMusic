package com.sharpcj.dreammusic.feature.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sharpcj.dreammusic.R
import com.sharpcj.dreammusic.core.model.LocalSong

private val SearchGreen = Color(0xFF31C27C)
private val SearchBackground = Color(0xFFF4F4F4)
private val SearchSecondary = Color(0xFF8B8878)
private val SearchDivider = Color(0xFFE8E8E8)

@Composable
fun SearchScreen(
    onOpenPlayer: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchBackground),
    ) {
        LegacySearchTopBar(
            query = uiState.query,
            onQueryChange = viewModel::updateQuery,
            onSearchClick = { if (uiState.hasResults) onOpenPlayer() },
        )
        SearchContent(
            uiState = uiState,
            onSongClick = viewModel::play,
            onToggleFavorite = viewModel::toggleFavorite,
            onHistoryKeywordClick = viewModel::applyHistoryKeyword,
            onDeleteHistoryKeyword = viewModel::deleteHistoryKeyword,
            onClearHistory = viewModel::clearHistory,
            onOpenPlayer = onOpenPlayer,
        )
    }
}

@Composable
private fun LegacySearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SearchGreen, Color(0xFF47D394)),
                ),
            )
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(26.dp),
                painter = painterResource(R.mipmap.top_tab_search_selected),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                shape = RoundedCornerShape(999.dp),
                leadingIcon = {
                    Image(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.mipmap.search1),
                        contentDescription = null,
                    )
                },
                placeholder = {
                    Text("歌曲、歌手、歌词、专辑", color = SearchSecondary)
                },
            )
            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clickable(onClick = onSearchClick),
                text = "搜索",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SearchContent(
    uiState: SearchUiState,
    onSongClick: (LocalSong) -> Unit,
    onToggleFavorite: (LocalSong) -> Unit,
    onHistoryKeywordClick: (String) -> Unit,
    onDeleteHistoryKeyword: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    when {
        uiState.isLibraryEmpty -> SearchEmptyMessage(
            title = "本地音乐库还是空的",
            message = "先到“音乐库”扫描本地音乐，搜索页会复用同一份本地歌曲缓存。",
        )
        !uiState.hasQuery -> SearchStartMessage(
            songCount = uiState.allSongs.size,
            recentKeywords = uiState.recentKeywords.map { it.keyword },
            onHistoryKeywordClick = onHistoryKeywordClick,
            onDeleteHistoryKeyword = onDeleteHistoryKeyword,
            onClearHistory = onClearHistory,
        )
        uiState.hasResults -> SearchResults(
            query = uiState.query.trim(),
            songs = uiState.results,
            favoriteSongIds = uiState.favoriteSongIds,
            onSongClick = onSongClick,
            onToggleFavorite = onToggleFavorite,
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
    favoriteSongIds: Set<Long>,
    onSongClick: (LocalSong) -> Unit,
    onToggleFavorite: (LocalSong) -> Unit,
    onOpenPlayer: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "“$query” 找到 ${songs.size} 首",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    modifier = Modifier.clickable(onClick = onOpenPlayer),
                    color = SearchGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        text = "播放器",
                        color = SearchGreen,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        items(items = songs, key = { it.id }) { song ->
            SearchSongRow(
                song = song,
                isFavorite = song.id in favoriteSongIds,
                onClick = { onSongClick(song) },
                onToggleFavorite = { onToggleFavorite(song) },
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun SearchSongRow(
    song: LocalSong,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SearchGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.mipmap.mainsearch),
                    contentDescription = null,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
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
                    color = SearchSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = "点击后按当前搜索结果队列播放 · ${formatDuration(song.durationMillis)}",
                    color = SearchSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            OutlinedButton(modifier = Modifier.padding(start = 8.dp), onClick = onToggleFavorite) {
                Text(if (isFavorite) "已喜欢" else "喜欢")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchStartMessage(
    songCount: Int,
    recentKeywords: List<String>,
    onHistoryKeywordClick: (String) -> Unit,
    onDeleteHistoryKeyword: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HotSearchCard(
                songCount = songCount,
                keywords = defaultHotKeywords(songCount, recentKeywords),
                onKeywordClick = onHistoryKeywordClick,
            )
        }
        if (recentKeywords.isNotEmpty()) {
            item {
                HistoryCard(
                    recentKeywords = recentKeywords,
                    onHistoryKeywordClick = onHistoryKeywordClick,
                    onDeleteHistoryKeyword = onDeleteHistoryKeyword,
                    onClearHistory = onClearHistory,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HotSearchCard(
    songCount: Int,
    keywords: List<String>,
    onKeywordClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp)) {
            Text(
                text = "热门搜索",
                color = SearchSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "当前音乐库共有 $songCount 首歌曲",
                color = SearchSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            FlowRow(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                keywords.forEach { keyword ->
                    SearchTag(text = keyword, onClick = { onKeywordClick(keyword) })
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryCard(
    recentKeywords: List<String>,
    onHistoryKeywordClick: (String) -> Unit,
    onDeleteHistoryKeyword: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "搜索历史", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    modifier = Modifier.clickable(onClick = onClearHistory),
                    text = "清空历史",
                    color = SearchGreen,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SearchDivider)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                recentKeywords.forEach { keyword ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SearchTag(text = keyword, onClick = { onHistoryKeywordClick(keyword) })
                        Text(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { onDeleteHistoryKeyword(keyword) },
                            text = "×",
                            color = SearchSecondary,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTag(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = SearchBackground,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            text = text,
            color = Color(0xFF333333),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SearchEmptyMessage(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = message,
            color = SearchSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun defaultHotKeywords(songCount: Int, recentKeywords: List<String>): List<String> {
    val defaults = listOf("周杰伦", "晴天", "陈奕迅", "林俊杰", "许嵩", "五月天", "独角戏", "本地音乐")
    return (recentKeywords + defaults + if (songCount > 0) listOf("全部歌曲") else emptyList()).distinct().take(12)
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
