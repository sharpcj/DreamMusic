package com.sharpcj.dreammusic.core.media

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sharpcj.dreammusic.core.data.PlaybackHistoryRepository
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class PlaybackController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
) {
    private val historyScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun play(song: LocalSong) {
        playQueue(songs = listOf(song), startIndex = 0)
    }

    fun playQueue(songs: List<LocalSong>, startIndex: Int) {
        if (songs.isEmpty()) return

        val clampedStartIndex = startIndex.coerceIn(songs.indices)
        historyScope.launch {
            playbackHistoryRepository.recordPlayed(songs[clampedStartIndex])
        }

        val intent = Intent(context, DreamMusicPlaybackService::class.java)
            .setAction(ACTION_PLAY_LOCAL_QUEUE)
            .putStringArrayListExtra(EXTRA_CONTENT_URIS, ArrayList(songs.map { it.contentUri.toString() }))
            .putStringArrayListExtra(EXTRA_TITLES, ArrayList(songs.map { it.title }))
            .putStringArrayListExtra(EXTRA_ARTISTS, ArrayList(songs.map { it.artist }))
            .putExtra(EXTRA_START_INDEX, clampedStartIndex)

        ContextCompat.startForegroundService(context, intent)
    }

    companion object {
        const val ACTION_PLAY_LOCAL_SONG = "com.sharpcj.dreammusic.action.PLAY_LOCAL_SONG"
        const val ACTION_PLAY_LOCAL_QUEUE = "com.sharpcj.dreammusic.action.PLAY_LOCAL_QUEUE"
        const val EXTRA_CONTENT_URI = "extra.CONTENT_URI"
        const val EXTRA_TITLE = "extra.TITLE"
        const val EXTRA_ARTIST = "extra.ARTIST"
        const val EXTRA_CONTENT_URIS = "extra.CONTENT_URIS"
        const val EXTRA_TITLES = "extra.TITLES"
        const val EXTRA_ARTISTS = "extra.ARTISTS"
        const val EXTRA_START_INDEX = "extra.START_INDEX"
    }
}
