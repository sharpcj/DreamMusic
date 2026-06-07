package com.sharpcj.dreammusic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sharpcj.dreammusic.core.model.SearchHistoryKeyword

@Entity(tableName = "search_history_keywords")
data class SearchHistoryKeywordEntity(
    @PrimaryKey val keyword: String,
    val searchedAtMillis: Long,
)

fun SearchHistoryKeywordEntity.asExternalModel(): SearchHistoryKeyword = SearchHistoryKeyword(
    keyword = keyword,
    searchedAtMillis = searchedAtMillis,
)
