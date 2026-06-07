package com.sharpcj.dreammusic.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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

@Serializable
internal data class BaiduBillboardResponse(
    @SerialName("song_list")
    val songList: List<BaiduBillboardSongDto> = emptyList(),
    val errorCode: Int? = null,
    val errorMessage: String? = null,
)

@Serializable
internal data class BaiduBillboardSongDto(
    @SerialName("song_id")
    val songId: String = "",
    val title: String = "",
    val author: String = "",
    @SerialName("album_title")
    val albumTitle: String = "",
    val rank: Int? = null,
) {
    fun toBillboardSong(index: Int): BillboardSong? {
        val normalizedTitle = title.trim()
        if (normalizedTitle.isEmpty()) return null

        return BillboardSong(
            id = songId.ifBlank { "remote-${index + 1}-$normalizedTitle" },
            title = normalizedTitle,
            artist = author.ifBlank { "未知歌手" },
            album = albumTitle.ifBlank { "未知专辑" },
            rank = rank ?: index + 1,
        )
    }
}
