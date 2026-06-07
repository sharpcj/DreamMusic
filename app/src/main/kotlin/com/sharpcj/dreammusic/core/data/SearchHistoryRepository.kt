package com.sharpcj.dreammusic.core.data

import com.sharpcj.dreammusic.core.database.SearchHistoryKeywordDao
import com.sharpcj.dreammusic.core.database.SearchHistoryKeywordEntity
import com.sharpcj.dreammusic.core.database.asExternalModel
import com.sharpcj.dreammusic.core.model.SearchHistoryKeyword
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class SearchHistoryRepository @Inject constructor(
    private val searchHistoryKeywordDao: SearchHistoryKeywordDao,
) {
    fun observeRecentKeywords(limit: Int = 12): Flow<List<SearchHistoryKeyword>> =
        searchHistoryKeywordDao.observeRecentKeywords(limit).map { entities ->
            entities.map { it.asExternalModel() }
        }

    suspend fun record(keyword: String) = withContext(Dispatchers.IO) {
        val normalizedKeyword = keyword.trim()
        if (normalizedKeyword.isNotBlank()) {
            searchHistoryKeywordDao.upsert(
                SearchHistoryKeywordEntity(
                    keyword = normalizedKeyword,
                    searchedAtMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun delete(keyword: String) = withContext(Dispatchers.IO) {
        searchHistoryKeywordDao.delete(keyword)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        searchHistoryKeywordDao.clear()
    }
}
