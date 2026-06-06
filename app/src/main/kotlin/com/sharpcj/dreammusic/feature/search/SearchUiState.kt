package com.sharpcj.dreammusic.feature.search

import com.sharpcj.dreammusic.core.model.LocalSong

data class SearchUiState(
    val query: String = "",
    val allSongs: List<LocalSong> = emptyList(),
    val results: List<LocalSong> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet(),
) {
    val hasQuery: Boolean = query.isNotBlank()
    val isLibraryEmpty: Boolean = allSongs.isEmpty()
    val hasResults: Boolean = results.isNotEmpty()
}
