package com.sharpcj.dreammusic.feature.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.FavoriteSongsRepository
import com.sharpcj.dreammusic.core.media.PlaybackController
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoriteSongsViewModel @Inject constructor(
    private val favoriteSongsRepository: FavoriteSongsRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val uiState = favoriteSongsRepository.observeFavoriteSongs()
        .map { FavoriteSongsUiState(items = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoriteSongsUiState(),
        )

    fun play(song: LocalSong) {
        val queue = uiState.value.items.map { it.song }
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = queue.ifEmpty { listOf(song) }, startIndex = startIndex)
    }

    fun playAll() {
        playbackController.playQueue(songs = uiState.value.items.map { it.song }, startIndex = 0)
    }

    fun removeFavorite(song: LocalSong) {
        viewModelScope.launch {
            favoriteSongsRepository.removeFavorite(song.id)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteSongsRepository.clear()
        }
    }
}
