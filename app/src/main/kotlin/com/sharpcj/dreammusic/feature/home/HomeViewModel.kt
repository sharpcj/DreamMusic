package com.sharpcj.dreammusic.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.FavoriteSongsRepository
import com.sharpcj.dreammusic.core.data.PlaybackHistoryRepository
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val favoriteSongsRepository: FavoriteSongsRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        playbackHistoryRepository.observeRecentPlayedSongs(),
        favoriteSongsRepository.observeFavoriteSongs(),
    ) { recentSongs, favoriteSongs ->
        HomeUiState(
            recentPlays = recentSongs.take(10),
            favoriteSongs = favoriteSongs.map { it.song }.take(10),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    fun refresh() {
        // Placeholder for manual refresh if needed
    }
}
