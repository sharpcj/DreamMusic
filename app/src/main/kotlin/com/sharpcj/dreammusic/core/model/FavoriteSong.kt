package com.sharpcj.dreammusic.core.model

data class FavoriteSong(
    val song: LocalSong,
    val favoritedAtMillis: Long,
)
