package com.sharpcj.dreammusic.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalSongDao {
    @Query("SELECT * FROM local_songs ORDER BY title COLLATE NOCASE ASC")
    fun observeLocalSongs(): Flow<List<LocalSongEntity>>

    @Upsert
    suspend fun upsertAll(songs: List<LocalSongEntity>)

    @Query("DELETE FROM local_songs WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Long>)

    @Query("DELETE FROM local_songs")
    suspend fun clear()
}
