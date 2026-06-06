package com.sharpcj.dreammusic.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs ORDER BY favoritedAtMillis DESC")
    fun observeFavoriteSongs(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT songId FROM favorite_songs")
    fun observeFavoriteSongIds(): Flow<List<Long>>

    @Upsert
    suspend fun upsert(song: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: Long)

    @Query("DELETE FROM favorite_songs")
    suspend fun clear()
}
