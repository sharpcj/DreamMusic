package com.sharpcj.dreammusic.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sharpcj.dreammusic.core.database.DreamMusicDatabase
import com.sharpcj.dreammusic.core.database.LocalSongDao
import com.sharpcj.dreammusic.core.database.RecentPlayedSongDao
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
    ).addMigrations(MIGRATION_1_2).build()

    @Provides
    fun provideLocalSongDao(database: DreamMusicDatabase): LocalSongDao = database.localSongDao()

    @Provides
    fun provideRecentPlayedSongDao(database: DreamMusicDatabase): RecentPlayedSongDao =
        database.recentPlayedSongDao()

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
}
