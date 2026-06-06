package com.sharpcj.dreammusic.feature.library

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.data.FavoriteSongsRepository
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
    private val favoriteSongsRepository: FavoriteSongsRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val refreshState = MutableStateFlow(RefreshState())
    private val sortMode = MutableStateFlow(LibrarySortMode.Title)
    private val groupMode = MutableStateFlow(LibraryGroupMode.None)

    val uiState = combine(
        localMusicRepository.observeLocalSongs(),
        favoriteSongsRepository.observeFavoriteSongIds(),
        refreshState,
        sortMode,
        groupMode,
    ) { songs, favoriteSongIds, refresh, sort, group ->
        val sortedSongs = songs.sortedWith(sort.comparator())
        LibraryUiState(
            songs = sortedSongs,
            songGroups = sortedSongs.groupBy(group),
            sortMode = sort,
            groupMode = group,
            favoriteSongIds = favoriteSongIds,
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

    fun setSortMode(mode: LibrarySortMode) {
        sortMode.value = mode
    }

    fun setGroupMode(mode: LibraryGroupMode) {
        groupMode.value = mode
    }

    fun play(song: LocalSong) {
        val songs = uiState.value.songs
        val startIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = songs.ifEmpty { listOf(song) }, startIndex = startIndex)
    }

    fun playQueue(songs: List<LocalSong>, startIndex: Int = 0) {
        playbackController.playQueue(songs = songs, startIndex = startIndex)
    }

    fun playFromQueue(song: LocalSong, songs: List<LocalSong>) {
        val queue = songs.ifEmpty { listOf(song) }
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackController.playQueue(songs = queue, startIndex = startIndex)
    }

    fun toggleFavorite(song: LocalSong) {
        val isFavorite = song.id in uiState.value.favoriteSongIds
        viewModelScope.launch {
            favoriteSongsRepository.toggleFavorite(song, isFavorite)
        }
    }

    fun groupSongs(mode: LibraryGroupMode, title: String): List<LocalSong> = when (mode) {
        LibraryGroupMode.None -> uiState.value.songs
        LibraryGroupMode.Artist -> uiState.value.songs.filter { it.groupArtist() == title }
        LibraryGroupMode.Album -> uiState.value.songs.filter { it.groupAlbum() == title }
    }

    private data class RefreshState(
        val isRefreshing: Boolean = false,
        val lastRefreshCount: Int? = null,
        val errorMessage: String? = null,
    )

    private fun LibrarySortMode.comparator(): Comparator<LocalSong> = when (this) {
        LibrarySortMode.Title -> compareBy(String.CASE_INSENSITIVE_ORDER, LocalSong::title)
            .thenBy(String.CASE_INSENSITIVE_ORDER, LocalSong::artist)
        LibrarySortMode.Artist -> compareBy(String.CASE_INSENSITIVE_ORDER, LocalSong::artist)
            .thenBy(String.CASE_INSENSITIVE_ORDER, LocalSong::title)
        LibrarySortMode.Album -> compareBy(String.CASE_INSENSITIVE_ORDER, LocalSong::album)
            .thenBy(String.CASE_INSENSITIVE_ORDER, LocalSong::title)
        LibrarySortMode.Duration -> compareBy<LocalSong> { it.durationMillis }
            .thenBy(String.CASE_INSENSITIVE_ORDER, LocalSong::title)
    }

    private fun List<LocalSong>.groupBy(mode: LibraryGroupMode): List<LibrarySongGroup> = when (mode) {
        LibraryGroupMode.None -> listOf(LibrarySongGroup(title = "全部歌曲", songs = this))
        LibraryGroupMode.Artist -> groupBy { it.groupArtist() }.toSongGroups()
        LibraryGroupMode.Album -> groupBy { it.groupAlbum() }.toSongGroups()
    }

    private fun LocalSong.groupArtist(): String = artist.ifBlank { "未知艺术家" }

    private fun LocalSong.groupAlbum(): String = album.ifBlank { "未知专辑" }

    private fun Map<String, List<LocalSong>>.toSongGroups(): List<LibrarySongGroup> =
        entries
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.key })
            .map { (title, songs) -> LibrarySongGroup(title = title, songs = songs) }
}
