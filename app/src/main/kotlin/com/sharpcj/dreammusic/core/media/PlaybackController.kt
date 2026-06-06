package com.sharpcj.dreammusic.core.media

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun play(song: LocalSong) {
        val intent = Intent(context, DreamMusicPlaybackService::class.java)
            .setAction(ACTION_PLAY_LOCAL_SONG)
            .putExtra(EXTRA_CONTENT_URI, song.contentUri.toString())
            .putExtra(EXTRA_TITLE, song.title)
            .putExtra(EXTRA_ARTIST, song.artist)

        ContextCompat.startForegroundService(context, intent)
    }

    companion object {
        const val ACTION_PLAY_LOCAL_SONG = "com.sharpcj.dreammusic.action.PLAY_LOCAL_SONG"
        const val EXTRA_CONTENT_URI = "extra.CONTENT_URI"
        const val EXTRA_TITLE = "extra.TITLE"
        const val EXTRA_ARTIST = "extra.ARTIST"
    }
}
