package com.sharpcj.dreammusic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalSongEntity::class,
        RecentPlayedSongEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class DreamMusicDatabase : RoomDatabase() {
    abstract fun localSongDao(): LocalSongDao
    abstract fun recentPlayedSongDao(): RecentPlayedSongDao
}
