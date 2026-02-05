package com.example.cursy.features.profile.data.remote.mapper

import com.example.cursy.features.feed.data.remote.dto.CourseDto
import com.example.cursy.features.profile.data.remote.dto.CourseItemDto
import com.example.cursy.features.profile.data.remote.dto.ProfileResponse
import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.entities.Profile

fun ProfileResponse.toDomain(): Profile {
    return Profile(
        id = user.id,
        name = user.name,
        email = user.email,
        password = user.password,
        profileImage = user.profileImage ?: "",
        bio = user.bio ?: "",
        university = user.university ?: "",
        publishedCoursesCount = stats.publishedCourses,
        draftCoursesCount = stats.draftCourses,
        savedCoursesCount = stats.savedCourses
    )
}

fun CourseItemDto.toDomain(): CourseItem {
    return CourseItem(
        id = id,
        title = title,
        description = description,
        coverImage = coverImage ?: "",
        isDraft = status == "DRAFT"
    )
}

fun CourseDto.toCourseItem(): CourseItem {
    return CourseItem(
        id = id,
        title = title,
        description = description,
        coverImage = coverImage ?: "",
        isDraft = status == "DRAFT"
    )
}