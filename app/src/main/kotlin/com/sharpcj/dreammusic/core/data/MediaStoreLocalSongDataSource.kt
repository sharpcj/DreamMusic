package com.sharpcj.dreammusic.core.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.sharpcj.dreammusic.core.model.LocalSong
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MediaStoreLocalSongDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun loadLocalSongs(): List<LocalSong> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        return context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    add(
                        LocalSong(
                            id = id,
                            title = cursor.getString(titleColumn).orUnknown("未知歌曲"),
                            album = cursor.getString(albumColumn).orUnknown("未知专辑"),
                            artist = cursor.getString(artistColumn).orUnknown("未知歌手"),
                            contentUri = ContentUris.withAppendedId(collection, id),
                            durationMillis = cursor.getLong(durationColumn),
                            sizeBytes = cursor.getLong(sizeColumn),
                        ),
                    )
                }
            }
        } ?: emptyList()
    }
}

private fun String?.orUnknown(fallback: String): String =
    takeUnless { it.isNullOrBlank() || it == MediaStore.UNKNOWN_STRING } ?: fallback
