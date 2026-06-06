package com.sharpcj.dreammusic.feature.recent

import com.sharpcj.dreammusic.core.model.RecentPlayedSong

data class RecentPlaysUiState(
    val items: List<RecentPlayedSong> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet(),
)
