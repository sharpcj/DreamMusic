package com.sharpcj.dreammusic.core.data

import com.sharpcj.dreammusic.core.database.RecentPlayedSongDao
import com.sharpcj.dreammusic.core.database.asRecentPlayedEntity
import com.sharpcj.dreammusic.core.model.LocalSong
import com.sharpcj.dreammusic.core.model.RecentPlayedSong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class PlaybackHistoryRepository @Inject constructor(
    private val recentPlayedSongDao: RecentPlayedSongDao,
) {
    fun observeRecentPlayedSongs(): Flow<List<RecentPlayedSong>> =
        recentPlayedSongDao.observeRecentPlayedSongs()
            .map { entities -> entities.map { it.asExternalModel() } }

    suspend fun recordPlayed(song: LocalSong) = withContext(Dispatchers.IO) {
        recentPlayedSongDao.upsert(song.asRecentPlayedEntity(System.currentTimeMillis()))
        recentPlayedSongDao.trimToLimit(RECENT_PLAY_LIMIT)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        recentPlayedSongDao.clear()
    }

    private companion object {
        const val RECENT_PLAY_LIMIT = 100
    }
}
