package com.example.cursy.core.network

import com.example.cursy.features.course.data.remote.dto.CourseDetailResponse
import com.example.cursy.features.feed.data.remote.dto.FeedResponse
import com.example.cursy.features.profile.data.remote.dto.MyCoursesResponse
import com.example.cursy.features.profile.data.remote.dto.ProfileResponse
import com.example.cursy.features.explore.data.remote.dto.UsersResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.*

interface CoursyApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterRequest

    @GET("courses")
    suspend fun getFeed(): FeedResponse

    @GET("courses/{id}")
    suspend fun getCourseDetail(@Path("id") courseId: String): CourseDetailResponse

    @POST("courses")
    suspend fun createCourse(@Body request: CreateCourseRequest): CreateCourseResponse

    @PUT("courses/{id}")
    suspend fun updateCourse(
        @Path("id") courseId: String,
        @Body request: UpdateCourseRequest
    ): MessageResponse

    @DELETE("courses/{id}")
    suspend fun deleteCourse(@Path("id") courseId: String): MessageResponse

    @PUT("courses/{id}/publish")
    suspend fun publishCourse(@Path("id") courseId: String): MessageResponse

    @POST("courses/{id}/save")
    suspend fun saveCourse(@Path("id") courseId: String): MessageResponse

    @DELETE("courses/{id}/save")
    suspend fun unsaveCourse(@Path("id") courseId: String): MessageResponse

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): FileUploadResponse

    @GET("profile")
    suspend fun getMyProfile(): ProfileResponse

    @GET("profile/courses")
    suspend fun getMyCourses(): MyCoursesResponse

    @GET("profile/saved")
    suspend fun getSavedCourses(): FeedResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): MessageResponse

    @DELETE("auth/account")
    suspend fun deleteAccount(): MessageResponse

    @GET("chats")
    suspend fun getConversations(): List<com.example.cursy.features.chat.data.remote.dto.ConversationDto>

    @POST("chats")
    suspend fun createConversation(@Body request: CreateConversationRequest): com.example.cursy.features.chat.data.remote.dto.ConversationDto

    @GET("chats/{id}/messages")
    suspend fun getMessages(@Path("id") conversationId: String): List<com.example.cursy.features.chat.data.remote.dto.MessageDto>

    @GET("users")
    suspend fun getUsers(@Query("q") query: String? = null): UsersResponse

    @GET("users/online")
    suspend fun getOnlineUsers(): OnlineUsersResponse

    @POST("chats/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): com.example.cursy.features.chat.data.remote.dto.MessageDto
}

data class UsersResponse(
    val users: List<UserResponse>,
    val count: Int
)

data class OnlineUsersResponse(
    @SerializedName("online_users")
    val onlineUsers: List<String>
)

data class SendMessageRequest(
    val content: String
)

data class CreateConversationRequest(
    val other_user_id: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val ine_url: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val bio: String?,
    val profile_image: String?,
    @SerializedName("university")
    val university: String?,
    val has_published_course: Boolean,
    val is_verified: Boolean
)

data class CreateCourseRequest(
    val title: String,
    val description: String,
    val cover_image: String? = null,
    val blocks: List<ContentBlockRequest>? = null
)

data class UpdateCourseRequest(
    val title: String? = null,
    val description: String? = null,
    val cover_image: String? = null,
    val blocks: List<ContentBlockRequest>? = null
)

data class ContentBlockRequest(
    val type: String,
    val content: String,
    val order: Int? = null
)

data class CreateCourseResponse(
    val message: String,
    val course: CourseCreatedDto
)

data class CourseCreatedDto(
    val id: String,
    val title: String,
    val description: String,
    val cover_image: String?,
    val status: String,
    val created_at: String
)

data class MessageResponse(
    val message: String
)

data class FileUploadResponse(
    val url: String
)

data class UpdateProfileRequest(
    val name: String? = null,
    val profile_image: String? = null,
    val bio: String? = null,
    @SerializedName("university")
    val university: String? = null
)