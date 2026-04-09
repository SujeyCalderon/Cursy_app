package com.example.cursy.features.feed.data.local.mapper

import com.example.cursy.features.feed.data.local.FeedCourseEntity
import com.example.cursy.features.feed.domain.entities.Course

fun Course.toFeedEntity(position: Int): FeedCourseEntity =
    FeedCourseEntity(
        id = id,
        authorId = authorId,
        title = title,
        description = description,
        coverImage = coverImage,
        authorName = authorName,
        authorImage = authorImage,
        position = position
    )

fun FeedCourseEntity.toDomain(): Course =
    Course(
        id = id,
        authorId = authorId,
        title = title,
        description = description,
        coverImage = coverImage,
        authorName = authorName,
        authorImage = authorImage
    )

fun List<FeedCourseEntity>.toCourseList(): List<Course> = map { it.toDomain() }
