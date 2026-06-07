package com.sharpcj.dreammusic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalSongEntity::class,
        RecentPlayedSongEntity::class,
        FavoriteSongEntity::class,
        SearchHistoryKeywordEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class DreamMusicDatabase : RoomDatabase() {
    abstract fun localSongDao(): LocalSongDao
    abstract fun recentPlayedSongDao(): RecentPlayedSongDao
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun searchHistoryKeywordDao(): SearchHistoryKeywordDao
}
