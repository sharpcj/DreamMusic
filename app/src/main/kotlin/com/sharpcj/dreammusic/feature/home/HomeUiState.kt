package com.sharpcj.dreammusic.feature.home

import com.sharpcj.dreammusic.core.model.LocalSong
import com.sharpcj.dreammusic.core.model.RecentPlayedSong

data class HomeUiState(
    val recentPlays: List<RecentPlayedSong> = emptyList(),
    val favoriteSongs: List<LocalSong> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
