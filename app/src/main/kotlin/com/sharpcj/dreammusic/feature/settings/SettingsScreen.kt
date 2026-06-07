package com.sharpcj.dreammusic.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

private val MoreGreen = Color(0xFF31C27C)
private val MoreBackground = Color(0xFFF4F4F4)
private val MoreSecondary = Color(0xFF9A9A9A)
private val MoreDivider = Color(0xFFE8E8E8)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.scanLocalMusic()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MoreBackground)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MoreHeader()
        MoreQuickEntrances()

        SettingsSection(title = "音乐库") {
            SettingsMetricRow(label = "本地歌曲", value = "${uiState.songCount} 首")
            uiState.lastScanCount?.let { count ->
                SettingsMetricRow(label = "最近扫描", value = "$count 首")
            }
            MoreActionRow(
                title = if (uiState.isScanning) "扫描中..." else "扫描本地音乐",
                subtitle = "重新读取设备中的音频文件",
                enabled = !uiState.isScanning,
                onClick = {
                    if (viewModel.hasMediaPermission()) {
                        viewModel.scanLocalMusic()
                    } else {
                        permissionLauncher.launch(viewModel.mediaPermission)
                    }
                },
            )
        }

        SettingsSection(title = "播放数据") {
            SettingsMetricRow(label = "最近播放", value = "${uiState.recentPlayCount} 首")
            SettingsMetricRow(label = "我喜欢", value = "${uiState.favoriteCount} 首")
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    enabled = uiState.recentPlayCount > 0,
                    onClick = viewModel::clearRecentPlays,
                ) {
                    Text("清空最近播放")
                }
                OutlinedButton(
                    enabled = uiState.favoriteCount > 0,
                    onClick = viewModel::clearFavorites,
                ) {
                    Text("清空喜欢")
                }
            }
        }

        SettingsSection(title = "关于") {
            SettingsMetricRow(label = "应用", value = "DreamMusic")
            SettingsMetricRow(label = "版本", value = "${uiState.appVersionName} (${uiState.appVersionCode})")
            SettingsMetricRow(label = "包名", value = "com.sharpcj.dreammusic")
        }

        uiState.statusMessage?.let { message ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                color = MoreGreen.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MoreGreen,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MoreHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MoreGreen, Color(0xFF46D394)),
                ),
            )
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(28.dp),
                painter = painterResource(R.mipmap.top_tab_more_selected),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "更多",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "会员、个性化、消息与应用管理",
                    color = Color.White.copy(alpha = 0.84f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MoreQuickEntrances() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MoreEntrance(
                iconRes = R.mipmap.more_icon_myvip_normal,
                title = "升级为VIP",
                subtitle = "畅想音乐特权",
            )
            MoreEntrance(
                iconRes = R.mipmap.more_icon_personal_center,
                title = "个性化中心",
                subtitle = "BIGBANG主题",
            )
            MoreEntrance(
                iconRes = R.mipmap.more_icon_notificationcenter,
                title = "消息中心",
                subtitle = "",
            )
        }
    }
}

@Composable
private fun MoreEntrance(iconRes: Int, title: String, subtitle: String) {
    Column(
        modifier = Modifier.width(108.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MoreGreen.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = Modifier.size(40.dp),
                painter = painterResource(iconRes),
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = title,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = subtitle,
            color = MoreSecondary,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            HorizontalDivider(color = MoreDivider)
            content()
        }
    }
}

@Composable
private fun SettingsMetricRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF333333))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MoreSecondary,
        )
    }
}

@Composable
private fun MoreActionRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MoreBackground)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.Black, fontWeight = FontWeight.Medium)
            Text(
                modifier = Modifier.padding(top = 3.dp),
                text = subtitle,
                color = MoreSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Image(
            modifier = Modifier.size(16.dp),
            painter = painterResource(R.mipmap.more_right_pressed),
            contentDescription = null,
        )
    }
}
