package com.sharpcj.dreammusic.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sharpcj.dreammusic.core.database.DreamMusicDatabase
import com.sharpcj.dreammusic.core.database.FavoriteSongDao
import com.sharpcj.dreammusic.core.database.LocalSongDao
import com.sharpcj.dreammusic.core.database.RecentPlayedSongDao
import com.sharpcj.dreammusic.core.database.SearchHistoryKeywordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDreamMusicDatabase(
        @ApplicationContext context: Context,
    ): DreamMusicDatabase = Room.databaseBuilder(
        context,
        DreamMusicDatabase::class.java,
        "dream_music.db",
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

    @Provides
    fun provideLocalSongDao(database: DreamMusicDatabase): LocalSongDao = database.localSongDao()

    @Provides
    fun provideRecentPlayedSongDao(database: DreamMusicDatabase): RecentPlayedSongDao =
        database.recentPlayedSongDao()

    @Provides
    fun provideFavoriteSongDao(database: DreamMusicDatabase): FavoriteSongDao = database.favoriteSongDao()

    @Provides
    fun provideSearchHistoryKeywordDao(database: DreamMusicDatabase): SearchHistoryKeywordDao =
        database.searchHistoryKeywordDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `recent_played_songs` (
                    `songId` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `album` TEXT NOT NULL,
                    `artist` TEXT NOT NULL,
                    `contentUri` TEXT NOT NULL,
                    `durationMillis` INTEGER NOT NULL,
                    `sizeBytes` INTEGER NOT NULL,
                    `playedAtMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`songId`)
                )
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `favorite_songs` (
                    `songId` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `album` TEXT NOT NULL,
                    `artist` TEXT NOT NULL,
                    `contentUri` TEXT NOT NULL,
                    `durationMillis` INTEGER NOT NULL,
                    `sizeBytes` INTEGER NOT NULL,
                    `favoritedAtMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`songId`)
                )
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `search_history_keywords` (
                    `keyword` TEXT NOT NULL,
                    `searchedAtMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`keyword`)
                )
                """.trimIndent(),
            )
        }
    }
}
