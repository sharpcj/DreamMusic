package com.sharpcj.dreammusic.feature.player

/**
 * Player 页面使用的轻量状态。
 *
 * 当前承接 Media3 Session 的播放状态、队列、播放模式和基础控制能力。
 */
data class PlayerUiState(
    val title: String = "暂无播放内容",
    val artist: String = "",
    val hasCurrentMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val canSkipToPrevious: Boolean = false,
    val canSkipToNext: Boolean = false,
    val playbackMode: PlaybackMode = PlaybackMode.Order,
    val queue: List<PlayerQueueItem> = emptyList(),
    val currentQueueIndex: Int = -1,
    val durationMillis: Long = 0L,
    val currentPositionMillis: Long = 0L,
    val isControllerReady: Boolean = false,
    val errorMessage: String? = null,
)

data class PlayerQueueItem(
    val mediaId: String,
    val title: String,
    val artist: String,
    val isCurrent: Boolean,
)

enum class PlaybackMode(val label: String) {
    Order("顺序播放"),
    RepeatOne("单曲循环"),
    Shuffle("随机播放"),
}
