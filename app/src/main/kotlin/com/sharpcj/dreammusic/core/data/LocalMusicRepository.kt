package com.sharpcj.dreammusic.core.data

import com.sharpcj.dreammusic.core.database.LocalSongDao
import com.sharpcj.dreammusic.core.database.asEntity
import com.sharpcj.dreammusic.core.model.LocalSong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class LocalMusicRepository @Inject constructor(
    private val localSongDao: LocalSongDao,
    private val mediaStoreLocalSongDataSource: MediaStoreLocalSongDataSource,
) {
    fun observeLocalSongs(): Flow<List<LocalSong>> =
        localSongDao.observeLocalSongs().map { entities -> entities.map { it.asExternalModel() } }

    suspend fun refreshLocalSongs(): Int = withContext(Dispatchers.IO) {
        val songs = mediaStoreLocalSongDataSource.loadLocalSongs()
        if (songs.isEmpty()) {
            localSongDao.clear()
        } else {
            localSongDao.upsertAll(songs.map { it.asEntity() })
            localSongDao.deleteMissing(songs.map { it.id })
        }
        songs.size
    }
}
