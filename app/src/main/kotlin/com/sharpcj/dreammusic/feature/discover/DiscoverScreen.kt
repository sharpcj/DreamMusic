package com.sharpcj.dreammusic.feature.discover

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
fun DiscoverScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "音乐馆", style = MaterialTheme.typography.headlineMedium)
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "后续这里会使用 ktor 获取榜单、精选和推荐歌单，并通过 Room 缓存。",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
