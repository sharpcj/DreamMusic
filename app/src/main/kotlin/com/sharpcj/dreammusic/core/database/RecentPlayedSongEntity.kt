package com.sharpcj.dreammusic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharpcj.dreammusic.core.model.LocalSong
import com.sharpcj.dreammusic.core.model.RecentPlayedSong

@Entity(tableName = "recent_played_songs")
data class RecentPlayedSongEntity(
    @PrimaryKey val songId: Long,
    val title: String,
    val album: String,
    val artist: String,
    val contentUri: String,
    val durationMillis: Long,
    val sizeBytes: Long,
    val playedAtMillis: Long,
) {
    fun asExternalModel() = RecentPlayedSong(
        song = LocalSong(
            id = songId,
            title = title,
            album = album,
            artist = artist,
            contentUri = android.net.Uri.parse(contentUri),
            durationMillis = durationMillis,
            sizeBytes = sizeBytes,
        ),
        playedAtMillis = playedAtMillis,
    )
}

fun LocalSong.asRecentPlayedEntity(playedAtMillis: Long) = RecentPlayedSongEntity(
    songId = id,
    title = title,
    album = album,
    artist = artist,
    contentUri = contentUri.toString(),
    durationMillis = durationMillis,
    sizeBytes = sizeBytes,
    playedAtMillis = playedAtMillis,
)
