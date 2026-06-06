package com.sharpcj.dreammusic.core.model

import android.net.Uri

/**
 * 应用内部使用的本地歌曲模型。
 *
 * 旧版 MediaStoreSong 里直接保存 DATA 文件路径。现代 Android 上更稳妥的做法是保存
 * MediaStore content Uri，播放和权限控制都交给系统媒体库处理。
 */
data class LocalSong(
    val id: Long,
    val title: String,
    val album: String,
    val artist: String,
    val contentUri: Uri,
    val durationMillis: Long,
    val sizeBytes: Long,
)
