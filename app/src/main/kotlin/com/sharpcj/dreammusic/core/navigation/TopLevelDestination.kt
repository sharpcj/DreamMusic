package com.sharpcj.dreammusic.core.navigation

import androidx.compose.runtime.Immutable

@Immutable
data class TopLevelDestination(
    val key: DreamMusicNavKey,
    val label: String,
)

val topLevelDestinations = listOf(
    TopLevelDestination(DreamMusicNavKey.Library, "本地音乐"),
    TopLevelDestination(DreamMusicNavKey.Discover, "音乐馆"),
    TopLevelDestination(DreamMusicNavKey.Search, "搜索"),
    TopLevelDestination(DreamMusicNavKey.Settings, "设置"),
)
