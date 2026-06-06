package com.sharpcj.dreammusic.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "播放器", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = uiState.artist.ifBlank { "未知艺术家" },
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        PlayerProgress(uiState = uiState)

        uiState.errorMessage?.let { message ->
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = viewModel::skipToPrevious,
                enabled = uiState.isControllerReady && uiState.canSkipToPrevious,
            ) {
                Text("上一首")
            }
            Button(
                onClick = viewModel::playOrPause,
                enabled = uiState.isControllerReady,
            ) {
                Text(if (uiState.isPlaying) "暂停" else "播放")
            }
            OutlinedButton(
                onClick = viewModel::skipToNext,
                enabled = uiState.isControllerReady && uiState.canSkipToNext,
            ) {
                Text("下一首")
            }
            OutlinedButton(
                onClick = viewModel::stop,
                enabled = uiState.isControllerReady,
            ) {
                Text("停止")
            }
            OutlinedButton(onClick = viewModel::refresh) { Text("刷新") }
        }

        OutlinedButton(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onBack,
        ) {
            Text("返回")
        }
    }
}

@Composable
private fun PlayerProgress(uiState: PlayerUiState) {
    val progress = if (uiState.durationMillis > 0L) {
        (uiState.currentPositionMillis.toFloat() / uiState.durationMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(formatDuration(uiState.currentPositionMillis), style = MaterialTheme.typography.bodySmall)
            Text(formatDuration(uiState.durationMillis), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis.coerceAtLeast(0) / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
