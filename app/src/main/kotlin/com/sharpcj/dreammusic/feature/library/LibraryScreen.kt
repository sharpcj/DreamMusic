package com.sharpcj.dreammusic.feature.library

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private val DreamGreen = Color(0xFF31C27C)
private val PageBackground = Color(0xFFF4F4F4)
private val DividerColor = Color(0xFFE8E8E8)
private val SecondaryText = Color(0xFF8E8E93)

@Composable
fun LibraryScreen(
    onOpenPlayer: () -> Unit,
    onOpenLocalMusic: () -> Unit,
    onOpenRecentPlays: () -> Unit,
    onOpenFavoriteSongs: () -> Unit,
    onOpenGroup: (LibraryGroupMode, String) -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            MyMusicHero(
                localCount = uiState.songs.size,
                favoriteCount = uiState.favoriteSongIds.size,
                isRefreshing = uiState.isRefreshing,
                onRefresh = {
                    if (viewModel.hasMediaPermission()) {
                        viewModel.refreshLocalSongs()
                    } else {
                        permissionLauncher.launch(viewModel.mediaPermission)
                    }
                },
                onOpenPlayer = onOpenPlayer,
            )
        }

        item {
            LegacyNoNetworkTip()
        }

        item {
            LegacySearchBar()
        }

        item {
            MyMusicMenuCard(
                localCount = uiState.songs.size,
                favoriteCount = uiState.favoriteSongIds.size,
                recentCount = null,
                downloadCount = null,
                onOpenLocal = onOpenLocalMusic,
                onOpenRecentPlays = onOpenRecentPlays,
                onOpenFavoriteSongs = onOpenFavoriteSongs,
            )
        }

        item {
            PlaylistCard()
        }

        uiState.lastRefreshCount?.let { count ->
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    text = "最近一次扫描到 $count 首歌曲",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        val errorMessage = uiState.errorMessage
        when {
            uiState.songs.isNotEmpty() -> {
                item {
                    LibraryViewControls(
                        uiState = uiState,
                        onSortModeSelected = viewModel::setSortMode,
                        onGroupModeSelected = viewModel::setGroupMode,
                    )
                }
                uiState.songGroups.forEach { group ->
                    if (uiState.groupMode != LibraryGroupMode.None) {
                        item(key = "group-${group.title}") {
                            GroupHeader(
                                title = group.title,
                                count = group.songs.size,
                                onClick = { onOpenGroup(uiState.groupMode, group.title) },
                            )
                        }
                    }
                    items(items = group.songs, key = { it.id }) { song ->
                        SongRow(
                            song = song,
                            isFavorite = song.id in uiState.favoriteSongIds,
                            onClick = { viewModel.play(song) },
                            onToggleFavorite = { viewModel.toggleFavorite(song) },
                        )
                    }
                }
            }
            errorMessage != null -> {
                item { EmptyLibraryMessage(errorMessage) }
            }
            else -> {
                item { EmptyLibraryMessage("点击“扫描本地音乐”读取设备上的音频文件。") }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MyMusicHero(
    localCount: Int,
    favoriteCount: Int,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenPlayer: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF28B874), DreamGreen, Color(0xFF4AD99A)),
                ),
            )
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.mipmap.top_tab_mymusic_selected),
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "我的音乐",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onOpenPlayer),
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        text = "播放器",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "保留旧版入口结构，用 Compose 重新打磨视觉层次。",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HeroStat(label = "本地歌曲", value = localCount.toString())
                HeroStat(label = "我喜欢", value = favoriteCount.toString())
                HeroAction(
                    text = if (isRefreshing) "扫描中..." else "扫描本地",
                    enabled = !isRefreshing,
                    onClick = onRefresh,
                )
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .width(92.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HeroAction(text: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 3.dp,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            text = text,
            color = DreamGreen,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LegacyNoNetworkTip() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        color = Color(0xFFFFF6D7),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.mipmap.net_work_connect_tips_warning),
                contentDescription = null,
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                text = "似乎没有网络噢，请检查网络设置",
                color = Color(0xFFF2A64A),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Image(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.mipmap.net_work_connect_tips_right),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Image(
                modifier = Modifier.size(14.dp),
                painter = painterResource(R.mipmap.net_work_connect_tips_cancel),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun LegacySearchBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        color = Color.White,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(R.mipmap.mainsearch),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "搜索", color = Color(0xFFC2C2C2), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MyMusicMenuCard(
    localCount: Int,
    favoriteCount: Int,
    recentCount: Int?,
    downloadCount: Int?,
    onOpenLocal: () -> Unit,
    onOpenRecentPlays: () -> Unit,
    onOpenFavoriteSongs: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            MyMusicMenuRow(
                iconRes = R.mipmap.mymusic_icon_allsongs_normal,
                title = "本地歌曲",
                countText = "$localCount",
                onClick = onOpenLocal,
            )
            MyMusicDivider()
            MyMusicMenuRow(
                iconRes = R.mipmap.mymusic_icon_download_normal,
                title = "最新下载",
                countText = downloadCount?.toString() ?: "--",
                onClick = {},
            )
            MyMusicDivider()
            MyMusicMenuRow(
                iconRes = R.mipmap.mymusic_icon_history_normal,
                title = "最近播放",
                countText = recentCount?.toString() ?: "查看",
                onClick = onOpenRecentPlays,
            )
            MyMusicDivider()
            MyMusicMenuRow(
                iconRes = R.mipmap.mymusic_icon_favorite_normal,
                title = "我喜欢",
                countText = "$favoriteCount",
                onClick = onOpenFavoriteSongs,
            )
        }
    }
}

@Composable
private fun MyMusicMenuRow(iconRes: Int, title: String, countText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier = Modifier.size(32.dp),
            painter = painterResource(iconRes),
            contentDescription = null,
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(text = countText, color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(10.dp))
        Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.mipmap.more_right_pressed),
            contentDescription = null,
        )
    }
}

@Composable
private fun MyMusicDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = DividerColor,
    )
}

@Composable
private fun PlaylistCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp),
                text = "我的歌单",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                thickness = 0.5.dp,
                color = DividerColor,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE6E6E6)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.mipmap.ic_add_folder),
                        contentDescription = null,
                    )
                }
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = "新建歌单",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun LibraryViewControls(
    uiState: LibraryUiState,
    onSortModeSelected: (LibrarySortMode) -> Unit,
    onGroupModeSelected: (LibraryGroupMode) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "本地歌曲视图",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "排序：${uiState.sortMode.label} · 分组：${uiState.groupMode.label}",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LibrarySortMode.entries.forEach { mode ->
                    ModeButton(
                        text = mode.label,
                        selected = uiState.sortMode == mode,
                        onClick = { onSortModeSelected(mode) },
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LibraryGroupMode.entries.forEach { mode ->
                    ModeButton(
                        text = mode.label,
                        selected = uiState.groupMode == mode,
                        onClick = { onGroupModeSelected(mode) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(text: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick) { Text(text) }
    }
}

@Composable
private fun GroupHeader(title: String, count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "$title · $count 首",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = DreamGreen,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "进入",
            style = MaterialTheme.typography.bodySmall,
            color = DreamGreen,
        )
    }
}

@Composable
private fun SongRow(
    song: LocalSong,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(DreamGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "♪", color = DreamGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = formatDuration(song.durationMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText,
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = onToggleFavorite),
                color = if (isFavorite) DreamGreen.copy(alpha = 0.12f) else PageBackground,
                shape = RoundedCornerShape(999.dp),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    text = if (isFavorite) "已喜欢" else "喜欢",
                    color = if (isFavorite) DreamGreen else SecondaryText,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun LocalMusicScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenGroup: (LibraryGroupMode, String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("单曲") }
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
            .background(PageBackground),
    ) {
        LegacyLocalMusicTopBar(onBack = onBack)
        LegacyLocalTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        when (selectedTab) {
            "单曲" -> LocalSongsTab(
                uiState = uiState,
                onRefresh = {
                    if (viewModel.hasMediaPermission()) {
                        viewModel.refreshLocalSongs()
                    } else {
                        permissionLauncher.launch(viewModel.mediaPermission)
                    }
                },
                onSongClick = viewModel::play,
                onToggleFavorite = viewModel::toggleFavorite,
            )
            "歌手" -> LocalGroupedTab(
                emptyMessage = "扫描本地歌曲后，会按歌手整理到这里。",
                groups = uiState.songs.groupBy { it.artist.ifBlank { "未知歌手" } },
                mode = LibraryGroupMode.Artist,
                onOpenGroup = onOpenGroup,
            )
            "专辑" -> LocalGroupedTab(
                emptyMessage = "扫描本地歌曲后，会按专辑整理到这里。",
                groups = uiState.songs.groupBy { it.album.ifBlank { "未知专辑" } },
                mode = LibraryGroupMode.Album,
                onOpenGroup = onOpenGroup,
            )
        }
    }
}

@Composable
private fun LegacyLocalMusicTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(DreamGreen),
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
            text = "本地歌曲",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LegacyLocalTabs(selectedTab: String, onTabSelected: (String) -> Unit) {
    val tabs = listOf("单曲", "歌手", "专辑")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        tabs.forEach { tab ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = tab,
                    color = if (selectedTab == tab) DreamGreen else Color(0xFF444444),
                    fontSize = 16.sp,
                    fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(32.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(if (selectedTab == tab) DreamGreen else Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun LocalSongsTab(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onSongClick: (LocalSong) -> Unit,
    onToggleFavorite: (LocalSong) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { LegacyLocalSearchBox() }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${uiState.songs.size} 首本地歌曲",
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                )
                Surface(
                    modifier = Modifier.clickable(enabled = !uiState.isRefreshing, onClick = onRefresh),
                    shape = RoundedCornerShape(999.dp),
                    color = DreamGreen.copy(alpha = 0.12f),
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        text = if (uiState.isRefreshing) "扫描中..." else "重新扫描",
                        color = DreamGreen,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        if (uiState.songs.isEmpty()) {
            item { EmptyLibraryMessage(uiState.errorMessage ?: "点击“重新扫描”读取设备上的音频文件。") }
        } else {
            items(items = uiState.songs, key = { it.id }) { song ->
                LegacyLocalSongRow(
                    song = song,
                    isFavorite = song.id in uiState.favoriteSongIds,
                    onClick = { onSongClick(song) },
                    onToggleFavorite = { onToggleFavorite(song) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun LegacyLocalSearchBox() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.mipmap.search1),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "搜索本地歌曲",
                color = Color(0xFFB8B8B8),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun LegacyLocalSongRow(
    song: LocalSong,
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
                    text = song.title,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(R.mipmap.known_artist_icon),
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = "${song.artist} · ${song.album}",
                        color = SecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = formatDuration(song.durationMillis),
                        color = SecondaryText,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable(onClick = onToggleFavorite),
                shape = RoundedCornerShape(999.dp),
                color = if (isFavorite) DreamGreen.copy(alpha = 0.12f) else PageBackground,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    text = if (isFavorite) "已喜欢" else "喜欢",
                    color = if (isFavorite) DreamGreen else SecondaryText,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.mipmap.more_version_arrow),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun LocalGroupedTab(
    emptyMessage: String,
    groups: Map<String, List<LocalSong>>,
    mode: LibraryGroupMode,
    onOpenGroup: (LibraryGroupMode, String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { LegacyLocalSearchBox() }
        if (groups.isEmpty()) {
            item { EmptyLibraryMessage(emptyMessage) }
        } else {
            items(items = groups.entries.sortedBy { it.key }, key = { it.key }) { entry ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenGroup(mode, entry.key) },
                    color = Color.White,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DreamGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (mode == LibraryGroupMode.Artist) "人" else "辑",
                                color = DreamGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        ) {
                            Text(
                                text = entry.key,
                                color = Color.Black,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                modifier = Modifier.padding(top = 3.dp),
                                text = "${entry.value.size} 首歌曲",
                                color = SecondaryText,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Image(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(R.mipmap.more_version_arrow),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun LibraryGroupDetailScreen(
    groupMode: LibraryGroupMode,
    groupTitle: String,
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val songs = viewModel.groupSongs(groupMode, groupTitle)
    val detailTitle = when (groupMode) {
        LibraryGroupMode.Artist -> "艺术家：$groupTitle"
        LibraryGroupMode.Album -> "专辑：$groupTitle"
        LibraryGroupMode.None -> groupTitle
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBackground)
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
            text = detailTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 6.dp),
            text = "${songs.size} 首歌曲 · 排序：${uiState.sortMode.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = songs.isNotEmpty(),
                onClick = { viewModel.playQueue(songs) },
            ) {
                Text("播放本组")
            }
            OutlinedButton(onClick = onBack) { Text("回到音乐库") }
        }

        if (songs.isEmpty()) {
            EmptyLibraryMessage("这个分组下暂时没有歌曲，可能需要重新扫描或切换分组。")
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = songs, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isFavorite = song.id in uiState.favoriteSongIds,
                        onClick = { viewModel.playFromQueue(song, songs) },
                        onToggleFavorite = { viewModel.toggleFavorite(song) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "当前会申请 ${Manifest.permission.READ_MEDIA_AUDIO} 权限。",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
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
