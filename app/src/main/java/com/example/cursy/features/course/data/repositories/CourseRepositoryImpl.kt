package com.example.cursy.features.course.data.repository

import com.example.cursy.core.network.ContentBlockRequest
import com.example.cursy.core.network.CoursyApi
import com.example.cursy.core.network.CreateCourseRequest
import com.example.cursy.features.course.data.remote.mapper.toDomain
import com.example.cursy.features.course.domain.entities.CourseDetail
import com.example.cursy.features.course.domain.repository.BlockInput
import com.example.cursy.features.course.domain.repository.CourseRepository
import com.example.cursy.features.course.domain.repository.CreateCourseInput
import com.example.cursy.features.course.domain.repository.UpdateCourseInput
import com.example.cursy.core.network.UpdateCourseRequest
import retrofit2.HttpException
import org.json.JSONObject
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class CourseRepositoryImpl(private val api: CoursyApi) : CourseRepository {

    override suspend fun getCourseDetail(courseId: String): Result<Triple<CourseDetail, Boolean, Boolean>> {
        return try {
            val response = api.getCourseDetail(courseId)
            val course = response.course.toDomain()
            Result.success(Triple(course, response.isOwner, response.isSaved))
        } catch (e: HttpException) {
            // Extraer mensaje del body del error HTTP
            val errorMessage = extractErrorMessage(e)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            api.deleteCourse(courseId)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCourse(input: CreateCourseInput): Result<String> {
        return try {
            val request = CreateCourseRequest(
                title = input.title,
                description = input.description,
                cover_image = input.coverImage,
                blocks = input.blocks?.map { block ->
                    ContentBlockRequest(
                        type = block.type,
                        content = block.content,
                        order = block.order
                    )
                }
            )
            val response = api.createCourse(request)
            Result.success(response.course.id)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishCourse(courseId: String): Result<Unit> {
        return try {
            api.publishCourse(courseId)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCourse(courseId: String, input: UpdateCourseInput): Result<Unit> {
        return try {
            val request = UpdateCourseRequest(
                title = input.title,
                description = input.description,
                cover_image = input.coverImage,
                blocks = input.blocks?.map { block ->
                    ContentBlockRequest(
                        type = block.type,
                        content = block.content,
                        order = block.order
                    )
                }
            )
            api.updateCourse(courseId, request)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCourse(courseId: String): Result<Unit> {
        return try {
            api.saveCourse(courseId)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unsaveCourse(courseId: String): Result<Unit> {
        return try {
            api.unsaveCourse(courseId)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(file: File): Result<String> {
        return try {
            val validExtension = file.extension.lowercase()
            val mediaTypeString = when {
                validExtension == "mp4" -> "video/mp4"
                validExtension in listOf("jpg", "jpeg") -> "image/jpeg"
                validExtension == "png" -> "image/png"
                else -> "application/octet-stream"
            }
            
            val requestFile = file.asRequestBody(mediaTypeString.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = api.uploadImage(body)
            Result.success(response.url)
        } catch (e: HttpException) {
            Result.failure(Exception(extractErrorMessage(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractErrorMessage(e: HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                val json = JSONObject(errorBody)
                // Intentar extraer "message" o "error" del JSON
                json.optString("message", null) 
                    ?: json.optString("error", null)
                    ?: "Error HTTP ${e.code()}"
            } else {
                "Error HTTP ${e.code()}"
            }
        } catch (ex: Exception) {
            "Error HTTP ${e.code()}: ${e.message()}"
        }
    }
}