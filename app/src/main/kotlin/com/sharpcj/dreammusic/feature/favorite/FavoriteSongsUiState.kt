package com.sharpcj.dreammusic.feature.favorite

import com.sharpcj.dreammusic.core.model.FavoriteSong

data class FavoriteSongsUiState(
    val items: List<FavoriteSong> = emptyList(),
)
