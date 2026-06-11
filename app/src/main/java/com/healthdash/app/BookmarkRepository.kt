package com.healthdash.app

import com.healthdash.app.data.AppDatabase
import com.healthdash.app.data.BookmarkEntity
import com.healthdash.app.data.HighlightEntity
import kotlinx.coroutines.flow.Flow

/** 북마크 / 하이라이트 Room CRUD */
class BookmarkRepository(private val db: AppDatabase) {

    val bookmarks: Flow<List<BookmarkEntity>> = db.bookmarkDao().getAll()
    val highlights: Flow<List<HighlightEntity>> = db.highlightDao().getAll()

    suspend fun addBookmark(bookmark: BookmarkEntity) = db.bookmarkDao().insert(bookmark)
    suspend fun deleteBookmark(bookmark: BookmarkEntity) = db.bookmarkDao().delete(bookmark)
    suspend fun addHighlight(highlight: HighlightEntity) = db.highlightDao().insert(highlight)
    suspend fun deleteHighlight(highlight: HighlightEntity) = db.highlightDao().delete(highlight)
}
