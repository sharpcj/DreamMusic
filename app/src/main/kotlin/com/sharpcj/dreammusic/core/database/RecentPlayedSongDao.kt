package com.sharpcj.dreammusic.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayedSongDao {
    @Query("SELECT * FROM recent_played_songs ORDER BY playedAtMillis DESC")
    fun observeRecentPlayedSongs(): Flow<List<RecentPlayedSongEntity>>

    @Upsert
    suspend fun upsert(song: RecentPlayedSongEntity)

    @Query(
        """
        DELETE FROM recent_played_songs
        WHERE songId NOT IN (
            SELECT songId FROM recent_played_songs
            ORDER BY playedAtMillis DESC
            LIMIT :limit
        )
        """,
    )
    suspend fun trimToLimit(limit: Int)

    @Query("DELETE FROM recent_played_songs")
    suspend fun clear()
}
