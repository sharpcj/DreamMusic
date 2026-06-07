package com.sharpcj.dreammusic.core.navigation

import androidx.navigation3.runtime.NavKey

sealed interface DreamMusicNavKey : NavKey {
    data object Library : DreamMusicNavKey
    data object Discover : DreamMusicNavKey
    data object Search : DreamMusicNavKey
    data object Settings : DreamMusicNavKey
    data object Player : DreamMusicNavKey
    data object LocalMusic : DreamMusicNavKey
    data object RecentPlays : DreamMusicNavKey
    data object FavoriteSongs : DreamMusicNavKey

    data class LibraryGroupDetail(
        val groupModeName: String,
        val groupTitle: String,
    ) : DreamMusicNavKey
}
