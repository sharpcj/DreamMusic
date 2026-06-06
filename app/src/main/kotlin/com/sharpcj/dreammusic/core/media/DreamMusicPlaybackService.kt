package com.sharpcj.dreammusic.core.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession.ControllerInfo

class DreamMusicPlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == PlaybackController.ACTION_PLAY_LOCAL_SONG) {
            playLocalSong(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun playLocalSong(intent: android.content.Intent) {
        val uri = intent.getStringExtra(PlaybackController.EXTRA_CONTENT_URI) ?: return
        val title = intent.getStringExtra(PlaybackController.EXTRA_TITLE).orEmpty()
        val artist = intent.getStringExtra(PlaybackController.EXTRA_ARTIST).orEmpty()
        val mediaItem = MediaItem.Builder()
            .setMediaId(uri)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title.ifBlank { "未知歌曲" })
                    .setArtist(artist.ifBlank { "未知艺术家" })
                    .build(),
            )
            .build()

        mediaSession?.player?.run {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }
}
