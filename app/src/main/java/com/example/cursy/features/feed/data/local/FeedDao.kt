package com.example.cursy.features.feed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FeedDao {

    @Query("SELECT * FROM feed_courses ORDER BY position ASC")
    suspend fun getAllOrdered(): List<FeedCourseEntity>

    @Query("DELETE FROM feed_courses")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FeedCourseEntity>)

    @Transaction
    suspend fun replaceAll(entities: List<FeedCourseEntity>) {
        clearAll()
        insertAll(entities)
    }
}
