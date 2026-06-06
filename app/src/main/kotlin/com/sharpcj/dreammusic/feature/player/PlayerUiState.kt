package com.sharpcj.dreammusic.feature.player

/**
 * Player 页面使用的轻量状态。
 *
 * 先只承接当前媒体信息和基础播放状态，后续再扩展队列、播放模式、进度拖拽等能力。
 */
data class PlayerUiState(
    val title: String = "暂无播放内容",
    val artist: String = "",
    val hasCurrentMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val durationMillis: Long = 0L,
    val currentPositionMillis: Long = 0L,
    val isControllerReady: Boolean = false,
    val errorMessage: String? = null,
)
