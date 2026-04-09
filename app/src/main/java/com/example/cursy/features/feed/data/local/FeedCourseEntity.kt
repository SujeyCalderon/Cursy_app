package com.example.cursy.features.feed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_courses")
data class FeedCourseEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val title: String,
    val description: String,
    val coverImage: String,
    val authorName: String,
    val authorImage: String,
    val position: Int
)
