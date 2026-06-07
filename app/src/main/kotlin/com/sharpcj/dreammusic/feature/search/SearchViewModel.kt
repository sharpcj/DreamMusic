package com.sharpcj.dreammusic.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.FavoriteSongsRepository
import com.sharpcj.dreammusic.core.data.LocalMusicRepository
import com.sharpcj.dreammusic.core.data.SearchHistoryRepository
import com.sharpcj.dreammusic.core.media.PlaybackController
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    localMusicRepository: LocalMusicRepository,
    private val favoriteSongsRepository: FavoriteSongsRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        localMusicRepository.observeLocalSongs(),
        favoriteSongsRepository.observeFavoriteSongIds(),
        searchHistoryRepository.observeRecentKeywords(),
    ) { rawQuery, songs, favoriteSongIds, recentKeywords ->
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
            favoriteSongIds = favoriteSongIds,
            recentKeywords = recentKeywords,
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

    fun applyHistoryKeyword(keyword: String) {
        query.value = keyword
        viewModelScope.launch {
            searchHistoryRepository.record(keyword)
        }
    }

    fun deleteHistoryKeyword(keyword: String) {
        viewModelScope.launch {
            searchHistoryRepository.delete(keyword)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clear()
        }
    }

    fun play(song: LocalSong) {
        val songs = uiState.value.results.ifEmpty { listOf(song) }
        val startIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = songs, startIndex = startIndex)
        viewModelScope.launch {
            searchHistoryRepository.record(uiState.value.query)
        }
    }

    fun toggleFavorite(song: LocalSong) {
        val isFavorite = song.id in uiState.value.favoriteSongIds
        viewModelScope.launch {
            favoriteSongsRepository.toggleFavorite(song, isFavorite)
        }
    }

    private fun LocalSong.matches(query: String): Boolean =
        title.contains(query, ignoreCase = true) ||
            artist.contains(query, ignoreCase = true)
}
