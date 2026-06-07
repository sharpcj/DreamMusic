package com.sharpcj.dreammusic.feature.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
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
import com.sharpcj.dreammusic.core.network.dto.BillboardSong
import com.sharpcj.dreammusic.core.network.dto.BillboardType

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "音乐馆", style = MaterialTheme.typography.headlineMedium)
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "通过 ktor 请求旧百度 Ting 榜单接口；接口不可用时展示明确错误，避免页面崩溃。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

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
private fun BillboardTabs(
    selectedType: BillboardType,
    onTypeSelected: (BillboardType) -> Unit,
) {
    LazyRow(
        modifier = Modifier.padding(top = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(BillboardType.entries) { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = { Text(type.displayName) },
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "正在加载榜单……", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "榜单暂时不可用", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = message,
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
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "榜单没有返回歌曲", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "旧接口可能已经调整或下线，可以稍后重试。",
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
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$title · ${songs.size} 首", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = onRefresh) { Text("刷新") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ElevatedCard {
            LazyColumn {
                items(songs, key = { it.id }) { song ->
                    BillboardSongRow(song = song)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun BillboardSongRow(song: BillboardSong) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(36.dp),
            text = song.rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
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
        }
    }
}
