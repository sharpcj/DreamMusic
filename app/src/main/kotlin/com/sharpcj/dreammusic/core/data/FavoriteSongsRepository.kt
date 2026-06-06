package com.sharpcj.dreammusic.core.data

import com.sharpcj.dreammusic.core.database.FavoriteSongDao
import com.sharpcj.dreammusic.core.database.asFavoriteEntity
import com.sharpcj.dreammusic.core.model.FavoriteSong
import com.sharpcj.dreammusic.core.model.LocalSong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class FavoriteSongsRepository @Inject constructor(
    private val favoriteSongDao: FavoriteSongDao,
) {
    fun observeFavoriteSongs(): Flow<List<FavoriteSong>> =
        favoriteSongDao.observeFavoriteSongs().map { entities -> entities.map { it.asExternalModel() } }

    fun observeFavoriteSongIds(): Flow<Set<Long>> =
        favoriteSongDao.observeFavoriteSongIds().map { it.toSet() }

    suspend fun addFavorite(song: LocalSong) = withContext(Dispatchers.IO) {
        favoriteSongDao.upsert(song.asFavoriteEntity(System.currentTimeMillis()))
    }

    suspend fun removeFavorite(songId: Long) = withContext(Dispatchers.IO) {
        favoriteSongDao.deleteBySongId(songId)
    }

    suspend fun toggleFavorite(song: LocalSong, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (isFavorite) {
            favoriteSongDao.deleteBySongId(song.id)
        } else {
            favoriteSongDao.upsert(song.asFavoriteEntity(System.currentTimeMillis()))
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        favoriteSongDao.clear()
    }
}
