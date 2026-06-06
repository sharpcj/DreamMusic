package com.sharpcj.dreammusic.feature.library

import com.sharpcj.dreammusic.core.model.LocalSong

data class LibraryUiState(
    val songs: List<LocalSong> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastRefreshCount: Int? = null,
    val errorMessage: String? = null,
)
