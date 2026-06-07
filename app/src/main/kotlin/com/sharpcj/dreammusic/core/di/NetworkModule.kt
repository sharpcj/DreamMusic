package com.sharpcj.dreammusic.core.di

import com.sharpcj.dreammusic.core.network.DreamMusicJson
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
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object NetworkProvidesModule {
    @Provides
    @Singleton
    fun provideJson(): Json = DreamMusicJson

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = createDreamMusicHttpClient(json)
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
