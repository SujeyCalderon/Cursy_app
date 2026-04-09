package com.example.cursy.features.feed.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FeedCourseEntity::class], version = 1, exportSchema = false)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
}
