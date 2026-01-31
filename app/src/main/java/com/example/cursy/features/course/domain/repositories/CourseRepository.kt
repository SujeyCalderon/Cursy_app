package com.example.cursy.features.course.domain.repository

import com.example.cursy.features.course.domain.entities.CourseDetail
import java.io.File

data class CreateCourseInput(
    val title: String,
    val description: String,
    val coverImage: String?,
    val blocks: List<BlockInput>?
)

data class UpdateCourseInput(
    val title: String?,
    val description: String?,
    val coverImage: String?,
    val blocks: List<BlockInput>?
)

data class BlockInput(
    val type: String,
    val content: String,
    val order: Int?
)

interface CourseRepository {
    suspend fun getCourseDetail(courseId: String): Result<Triple<CourseDetail, Boolean, Boolean>>
    suspend fun deleteCourse(courseId: String): Result<Unit>
    suspend fun createCourse(input: CreateCourseInput): Result<String>
    suspend fun publishCourse(courseId: String): Result<Unit>
    suspend fun updateCourse(courseId: String, input: UpdateCourseInput): Result<Unit>
    suspend fun saveCourse(courseId: String): Result<Unit>
    suspend fun unsaveCourse(courseId: String): Result<Unit>
    suspend fun uploadImage(file: File): Result<String>
}