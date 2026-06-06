package com.sharpcj.dreammusic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharpcj.dreammusic.core.model.FavoriteSong
import com.sharpcj.dreammusic.core.model.LocalSong

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val songId: Long,
    val title: String,
    val album: String,
    val artist: String,
    val contentUri: String,
    val durationMillis: Long,
    val sizeBytes: Long,
    val favoritedAtMillis: Long,
) {
    fun asExternalModel() = FavoriteSong(
        song = LocalSong(
            id = songId,
            title = title,
            album = album,
            artist = artist,
            contentUri = android.net.Uri.parse(contentUri),
            durationMillis = durationMillis,
            sizeBytes = sizeBytes,
        ),
        favoritedAtMillis = favoritedAtMillis,
    )
}

fun LocalSong.asFavoriteEntity(favoritedAtMillis: Long) = FavoriteSongEntity(
    songId = id,
    title = title,
    album = album,
    artist = artist,
    contentUri = contentUri.toString(),
    durationMillis = durationMillis,
    sizeBytes = sizeBytes,
    favoritedAtMillis = favoritedAtMillis,
)
