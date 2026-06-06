package com.sharpcj.dreammusic.feature.player

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sharpcj.dreammusic.core.media.DreamMusicPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateFrom(player)
        }
    }

    init {
        connectController()
    }

    fun playOrPause() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        updateFrom(player)
    }

    fun stop() {
        val player = controller ?: return
        player.stop()
        updateFrom(player)
    }

    fun skipToPrevious() {
        val player = controller ?: return
        player.seekToPreviousMediaItem()
        updateFrom(player)
    }

    fun skipToNext() {
        val player = controller ?: return
        player.seekToNextMediaItem()
        updateFrom(player)
    }

    fun seekTo(positionMillis: Long) {
        val player = controller ?: return
        player.seekTo(positionMillis.coerceAtLeast(0L))
        updateFrom(player)
    }

    fun playQueueItem(index: Int) {
        val player = controller ?: return
        if (index !in 0 until player.mediaItemCount) return
        player.seekTo(index, 0L)
        player.play()
        updateFrom(player)
    }

    fun cyclePlaybackMode() {
        val player = controller ?: return
        when (currentPlaybackMode(player)) {
            PlaybackMode.Order -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }
            PlaybackMode.RepeatOne -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = true
            }
            PlaybackMode.Shuffle -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
            }
        }
        updateFrom(player)
    }

    fun refresh() {
        controller?.let(::updateFrom)
    }

    override fun onCleared() {
        progressJob?.cancel()
        controller?.removeListener(playerListener)
        controllerFuture?.let(MediaController::releaseFuture)
        controller = null
        controllerFuture = null
        super.onCleared()
    }

    private fun connectController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(
            context,
            ComponentName(context, DreamMusicPlaybackService::class.java),
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { mediaController ->
                        controller = mediaController
                        mediaController.addListener(playerListener)
                        updateFrom(mediaController)
                        startProgressTicker()
                    }
                    .onFailure { throwable ->
                        _uiState.value = _uiState.value.copy(
                            isControllerReady = false,
                            errorMessage = throwable.message ?: "连接播放器失败",
                        )
                    }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                controller?.let(::updateFrom)
                delay(1_000)
            }
        }
    }

    private fun updateFrom(player: Player) {
        val metadata = player.mediaMetadata
        val currentIndex = player.currentMediaItemIndex.takeIf { it >= 0 } ?: -1
        _uiState.value = PlayerUiState(
            title = metadata.displayTitleOrTitle(),
            artist = metadata.artist?.toString().orEmpty(),
            hasCurrentMedia = player.currentMediaItem != null || player.mediaItemCount > 0,
            isPlaying = player.isPlaying,
            canSkipToPrevious = player.hasPreviousMediaItem(),
            canSkipToNext = player.hasNextMediaItem(),
            playbackMode = currentPlaybackMode(player),
            queue = player.queueItems(currentIndex),
            currentQueueIndex = currentIndex,
            durationMillis = player.duration.takeIf { it > 0 } ?: 0L,
            currentPositionMillis = player.currentPosition.coerceAtLeast(0L),
            isControllerReady = true,
        )
    }

    private fun currentPlaybackMode(player: Player): PlaybackMode = when {
        player.shuffleModeEnabled -> PlaybackMode.Shuffle
        player.repeatMode == Player.REPEAT_MODE_ONE -> PlaybackMode.RepeatOne
        else -> PlaybackMode.Order
    }

    private fun Player.queueItems(currentIndex: Int): List<PlayerQueueItem> =
        (0 until mediaItemCount).map { index ->
            val item = getMediaItemAt(index)
            val metadata = item.mediaMetadata
            PlayerQueueItem(
                mediaId = item.mediaId,
                title = metadata.displayTitleOrTitle(),
                artist = metadata.artist?.toString().orEmpty(),
                isCurrent = index == currentIndex,
            )
        }

    private fun MediaMetadata.displayTitleOrTitle(): String =
        displayTitle?.toString()
            ?: title?.toString()
            ?: "暂无播放内容"
}
