package com.sharpcj.dreammusic.core.model

data class RecentPlayedSong(
    val song: LocalSong,
    val playedAtMillis: Long,
)
