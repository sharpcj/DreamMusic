package com.sharpcj.dreammusic.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LibraryScreen(onOpenPlayer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "本地音乐", style = MaterialTheme.typography.headlineMedium)
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "阶段 1：Compose + Navigation3 应用壳。后续这里会接入 MediaStore、Room 和播放队列。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(modifier = Modifier.padding(top = 24.dp), onClick = onOpenPlayer) { Text("打开播放器") }
    }
}
