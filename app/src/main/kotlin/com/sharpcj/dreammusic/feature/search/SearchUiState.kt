package com.sharpcj.dreammusic.feature.search

import com.sharpcj.dreammusic.core.model.LocalSong
import com.sharpcj.dreammusic.core.model.SearchHistoryKeyword

data class SearchUiState(
    val query: String = "",
    val allSongs: List<LocalSong> = emptyList(),
    val results: List<LocalSong> = emptyList(),
    val favoriteSongIds: Set<Long> = emptySet(),
    val recentKeywords: List<SearchHistoryKeyword> = emptyList(),
) {
    val hasQuery: Boolean = query.isNotBlank()
    val isLibraryEmpty: Boolean = allSongs.isEmpty()
    val hasResults: Boolean = results.isNotEmpty()
}
