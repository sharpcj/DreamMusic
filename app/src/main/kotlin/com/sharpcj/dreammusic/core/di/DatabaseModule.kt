package com.sharpcj.dreammusic.core.di

import android.content.Context
import androidx.room.Room
import com.sharpcj.dreammusic.core.database.DreamMusicDatabase
import com.sharpcj.dreammusic.core.database.LocalSongDao
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
    ).build()

    @Provides
    fun provideLocalSongDao(database: DreamMusicDatabase): LocalSongDao = database.localSongDao()
}
