package com.sharpcj.dreammusic.feature.discover

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sharpcj.dreammusic.core.network.dto.BillboardSong
import com.sharpcj.dreammusic.core.network.dto.BillboardType

private val HallGreen = Color(0xFF31C27C)
private val HallBackground = Color(0xFFF4F4F4)
private val HallSecondary = Color(0xFF80000000)
private val HallDivider = Color(0xFFE0E0E0)

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HallBackground),
    ) {
        MusicHallHeader(onRefresh = viewModel::refresh)
        BillboardTabs(
            selectedType = uiState.selectedBillboardType,
            onTypeSelected = viewModel::selectBillboard,
        )

        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::refresh,
            )
            uiState.isEmpty -> EmptyState(onRetry = viewModel::refresh)
            else -> BillboardSongList(
                title = uiState.selectedBillboardType.displayName,
                songs = uiState.billboardSongs,
                onRefresh = viewModel::refresh,
            )
        }
    }
}

@Composable
private fun MusicHallHeader(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HallGreen, Color(0xFF45D394)),
                ),
            )
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.size(28.dp),
                    painter = painterResource(R.mipmap.top_tab_musichall_selected),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "音乐馆",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = "榜单 · 热歌 · 新歌，一次听个够",
                        color = Color.White.copy(alpha = 0.84f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Surface(
                modifier = Modifier.clickable(onClick = onRefresh),
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    text = "刷新",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun BillboardTabs(
    selectedType: BillboardType,
    onTypeSelected: (BillboardType) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(BillboardType.entries) { type ->
            Surface(
                modifier = Modifier.clickable { onTypeSelected(type) },
                color = if (type == selectedType) HallGreen else HallBackground,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    text = type.displayName,
                    color = if (type == selectedType) Color.White else Color(0xFF333333),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (type == selectedType) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "正在加载榜单……", style = MaterialTheme.typography.titleMedium, color = HallSecondary)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "榜单暂时不可用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = message,
            color = HallSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(modifier = Modifier.padding(top = 16.dp), onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "榜单没有返回歌曲", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "旧接口可能已经调整或下线，可以稍后重试。",
            color = HallSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(modifier = Modifier.padding(top = 16.dp), onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
private fun BillboardSongList(
    title: String,
    songs: List<BillboardSong>,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = "${songs.size} 首歌曲",
                        color = HallSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onRefresh),
                    color = HallGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        text = "刷新榜单",
                        color = HallGreen,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        items(songs, key = { it.id }) { song ->
            BillboardSongRow(song = song)
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun BillboardSongRow(song: BillboardSong) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 15.dp, top = 10.dp, end = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(HallGreen.copy(alpha = 0.88f), Color(0xFF7CE6B7)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier.size(34.dp),
                            painter = painterResource(R.mipmap.ic_launcher),
                            contentDescription = null,
                        )
                    }
                    RankBadge(rank = song.rank)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                ) {
                    Text(
                        text = song.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(top = 10.dp),
                        text = song.artist,
                        color = HallSecondary,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Image(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .size(24.dp),
                    painter = painterResource(R.mipmap.bt_recsong_more),
                    contentDescription = null,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 15.dp, end = 30.dp),
                color = HallDivider,
                thickness = 0.5.dp,
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val badgeColor = when (rank) {
        1 -> Color(0xFFFF5A5F)
        2 -> Color(0xFFFFA726)
        3 -> Color(0xFFFFD54F)
        else -> Color(0x99000000)
    }
    Surface(
        modifier = Modifier.size(35.dp),
        color = badgeColor,
        shape = CircleShape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = rank.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
