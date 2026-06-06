package com.sharpcj.dreammusic.feature.library

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.LocalMusicRepository
import com.sharpcj.dreammusic.core.media.PlaybackController
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val application: Application,
    private val localMusicRepository: LocalMusicRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())

    val uiState = combine(
        localMusicRepository.observeLocalSongs(),
        refreshState,
    ) { songs, refresh ->
        LibraryUiState(
            songs = songs,
            isRefreshing = refresh.isRefreshing,
            lastRefreshCount = refresh.lastRefreshCount,
            errorMessage = refresh.errorMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    val mediaPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasMediaPermission(): Boolean = ContextCompat.checkSelfPermission(
        application,
        mediaPermission,
    ) == PackageManager.PERMISSION_GRANTED

    fun refreshLocalSongs() {
        viewModelScope.launch {
            if (!hasMediaPermission()) {
                refreshState.value = RefreshState(errorMessage = "需要授权读取音频文件后才能扫描本地音乐")
                return@launch
            }

            refreshState.value = RefreshState(isRefreshing = true)
            runCatching { localMusicRepository.refreshLocalSongs() }
                .onSuccess { count -> refreshState.value = RefreshState(lastRefreshCount = count) }
                .onFailure { throwable ->
                    refreshState.value = RefreshState(errorMessage = throwable.message ?: "扫描本地音乐失败")
                }
        }
    }

    fun play(song: LocalSong) {
        val songs = uiState.value.songs
        val startIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = songs.ifEmpty { listOf(song) }, startIndex = startIndex)
    }

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val lastRefreshCount: Int? = null,
        val errorMessage: String? = null,
    )
}
