package com.sharpcj.dreammusic.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = "设置", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "集中管理本地音乐扫描、播放数据和应用信息。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingsSection(title = "音乐库") {
            SettingsMetricRow(label = "本地歌曲", value = "${uiState.songCount} 首")
            uiState.lastScanCount?.let { count ->
                SettingsMetricRow(label = "最近扫描", value = "$count 首")
            }
            Button(
                modifier = Modifier.padding(top = 12.dp),
                enabled = !uiState.isScanning,
                onClick = {
                    if (viewModel.hasMediaPermission()) {
                        viewModel.scanLocalMusic()
                    } else {
                        permissionLauncher.launch(viewModel.mediaPermission)
                    }
                },
            ) {
                Text(if (uiState.isScanning) "扫描中..." else "扫描本地音乐")
            }
        }

        SettingsSection(title = "播放数据") {
            SettingsMetricRow(label = "最近播放", value = "${uiState.recentPlayCount} 首")
            SettingsMetricRow(label = "我喜欢", value = "${uiState.favoriteCount} 首")
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        HorizontalDivider()
        content()
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
