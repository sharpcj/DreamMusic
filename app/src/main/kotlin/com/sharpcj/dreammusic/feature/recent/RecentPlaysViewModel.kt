package com.sharpcj.dreammusic.feature.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.PlaybackHistoryRepository
import com.sharpcj.dreammusic.core.media.PlaybackController
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecentPlaysViewModel @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val uiState = playbackHistoryRepository.observeRecentPlayedSongs()
        .map { RecentPlaysUiState(items = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecentPlaysUiState(),
        )

    fun play(song: LocalSong) {
        val queue = uiState.value.items.map { it.song }
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = queue.ifEmpty { listOf(song) }, startIndex = startIndex)
    }

    fun playAll() {
        val queue = uiState.value.items.map { it.song }
        playbackController.playQueue(songs = queue, startIndex = 0)
    }

    fun clearHistory() {
        viewModelScope.launch {
            playbackHistoryRepository.clear()
        }
    }
}
