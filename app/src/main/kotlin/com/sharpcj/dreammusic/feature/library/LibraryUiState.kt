package com.sharpcj.dreammusic.feature.library

import com.sharpcj.dreammusic.core.model.LocalSong

data class LibraryUiState(
    val songs: List<LocalSong> = emptyList(),
    val songGroups: List<LibrarySongGroup> = emptyList(),
    val sortMode: LibrarySortMode = LibrarySortMode.Title,
    val groupMode: LibraryGroupMode = LibraryGroupMode.None,
    val favoriteSongIds: Set<Long> = emptySet(),
    val isRefreshing: Boolean = false,
    val lastRefreshCount: Int? = null,
    val errorMessage: String? = null,
)

data class LibrarySongGroup(
    val title: String,
    val songs: List<LocalSong>,
)

enum class LibrarySortMode(val label: String) {
    Title("歌曲名"),
    Artist("艺术家"),
    Album("专辑"),
    Duration("时长"),
}

enum class LibraryGroupMode(val label: String) {
    None("不分组"),
    Artist("按艺术家"),
    Album("按专辑"),
}
