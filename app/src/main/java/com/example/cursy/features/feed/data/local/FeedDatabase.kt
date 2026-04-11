package com.example.cursy.features.feed.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// david: Agregando DownloadEntity, su DAO y los Converters necesarios a la base de datos
@Database(entities = [FeedCourseEntity::class, DownloadEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FeedDatabase : RoomDatabase() {
    abstract fun feedDao(): FeedDao
    abstract fun downloadDao(): DownloadDao
}
