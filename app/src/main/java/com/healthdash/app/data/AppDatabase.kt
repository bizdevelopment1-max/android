package com.healthdash.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BookmarkEntity>>

    @Insert
    suspend fun insert(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights ORDER BY createdAt DESC")
    fun getAll(): Flow<List<HighlightEntity>>

    @Insert
    suspend fun insert(highlight: HighlightEntity)

    @Delete
    suspend fun delete(highlight: HighlightEntity)
}

@Database(
    entities = [BookmarkEntity::class, HighlightEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "healthdash.db"
            ).build().also { instance = it }
        }
    }
}
