package com.sharpcj.dreammusic.core.di

import com.sharpcj.dreammusic.core.network.createDreamMusicHttpClient
import com.sharpcj.dreammusic.core.network.datasource.BaiduMusicRemoteDataSource
import com.sharpcj.dreammusic.core.network.datasource.RemoteMusicDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvidesModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = createDreamMusicHttpClient()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingsModule {
    @Binds
    @Singleton
    abstract fun bindRemoteMusicDataSource(
        dataSource: BaiduMusicRemoteDataSource,
    ): RemoteMusicDataSource
}
