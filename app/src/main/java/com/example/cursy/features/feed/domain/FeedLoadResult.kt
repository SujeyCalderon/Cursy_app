package com.example.cursy.features.feed.domain

import com.example.cursy.features.feed.domain.entities.Course

data class FeedLoadResult(
    val courses: List<Course>,
    val fromCache: Boolean
)
