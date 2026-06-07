package com.sharpcj.dreammusic.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryKeywordDao {
    @Query("SELECT * FROM search_history_keywords ORDER BY searchedAtMillis DESC LIMIT :limit")
    fun observeRecentKeywords(limit: Int = 12): Flow<List<SearchHistoryKeywordEntity>>

    @Upsert
    suspend fun upsert(keyword: SearchHistoryKeywordEntity)

    @Query("DELETE FROM search_history_keywords WHERE keyword = :keyword")
    suspend fun delete(keyword: String)

    @Query("DELETE FROM search_history_keywords")
    suspend fun clear()
}
