package com.sharpcj.dreammusic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharpcj.dreammusic.core.model.LocalSong

@Entity(tableName = "local_songs")
data class LocalSongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val album: String,
    val artist: String,
    val contentUri: String,
    val durationMillis: Long,
    val sizeBytes: Long,
) {
    fun asExternalModel() = LocalSong(
        id = id,
        title = title,
        album = album,
        artist = artist,
        contentUri = android.net.Uri.parse(contentUri),
        durationMillis = durationMillis,
        sizeBytes = sizeBytes,
    )
}

fun LocalSong.asEntity() = LocalSongEntity(
    id = id,
    title = title,
    album = album,
    artist = artist,
    contentUri = contentUri.toString(),
    durationMillis = durationMillis,
    sizeBytes = sizeBytes,
)
