package com.sharpcj.dreammusic.feature.search

import androidx.lifecycle.ViewModel
import com.sharpcj.dreammusic.core.data.LocalMusicRepository
import com.sharpcj.dreammusic.core.media.PlaybackController
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

@HiltViewModel
class SearchViewModel @Inject constructor(
    localMusicRepository: LocalMusicRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        localMusicRepository.observeLocalSongs(),
    ) { rawQuery, songs ->
        val normalizedQuery = rawQuery.trim()
        val results = if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            songs.filter { song -> song.matches(normalizedQuery) }
        }
        SearchUiState(
            query = rawQuery,
            allSongs = songs,
            results = results,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    fun updateQuery(value: String) {
        query.value = value
    }

    fun clearQuery() {
        query.value = ""
    }

    fun play(song: LocalSong) {
        val songs = uiState.value.results.ifEmpty { listOf(song) }
        val startIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = songs, startIndex = startIndex)
    }

    private fun LocalSong.matches(query: String): Boolean =
        title.contains(query, ignoreCase = true) ||
            artist.contains(query, ignoreCase = true)
}
