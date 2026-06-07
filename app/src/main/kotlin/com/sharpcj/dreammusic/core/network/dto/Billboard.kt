package com.sharpcj.dreammusic.core.network.dto

data class BillboardSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val rank: Int,
)

data class Billboard(
    val type: BillboardType,
    val songs: List<BillboardSong>,
)

enum class BillboardType(
    val code: Int,
    val displayName: String,
) {
    Hot(code = 2, displayName = "热歌榜"),
    New(code = 1, displayName = "新歌榜"),
    Classic(code = 22, displayName = "经典老歌榜"),
    Internet(code = 25, displayName = "网络歌曲榜"),
}
