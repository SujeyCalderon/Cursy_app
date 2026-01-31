package com.example.cursy.core.di

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.course.data.repository.CourseRepositoryImpl
import com.example.cursy.features.course.domain.repository.CourseRepository
import com.example.cursy.features.course.domain.usecases.CreateCourseUseCase
import com.example.cursy.features.course.domain.usecases.DeleteCourseUseCase
import com.example.cursy.features.course.domain.usecases.GetCourseDetailUseCase
import com.example.cursy.features.course.domain.usecases.SaveCourseUseCase
import com.example.cursy.features.course.domain.usecases.UpdateCourseUseCase
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.feed.data.repository.FeedRepositoryImpl
import com.example.cursy.features.feed.domain.repository.FeedRepository
import com.example.cursy.features.feed.domain.usecases.GetFeedUseCase
import com.example.cursy.features.profile.data.repository.ProfileRepositoryImpl
import com.example.cursy.features.profile.domain.repository.ProfileRepository
import com.example.cursy.features.profile.domain.usecases.GetMyCoursesUseCase
import com.example.cursy.features.profile.domain.usecases.GetMyProfileUseCase
import com.example.cursy.features.profile.domain.usecases.GetSavedCoursesUseCase
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer {

    // Token de autenticación
    private var authToken: String? = null
    private var currentUserId: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun getAuthToken(): String? = authToken
    
    fun setCurrentUserId(userId: String) {
        currentUserId = userId
    }
    
    fun getCurrentUserId(): String? = currentUserId

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor que agrega el header de Authorization
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        
        authToken?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://52.20.206.74:8080/api/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val coursyApi: CoursyApi by lazy {
        retrofit.create(CoursyApi::class.java)
    }

    // Repositories
    val feedRepository: FeedRepository by lazy {
        FeedRepositoryImpl(coursyApi)
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepositoryImpl(coursyApi)
    }

    val courseRepository: CourseRepository by lazy {
        CourseRepositoryImpl(coursyApi)
    }

    // Use Cases
    val getFeedUseCase: GetFeedUseCase by lazy {
        GetFeedUseCase(feedRepository)
    }

    val getMyProfileUseCase: GetMyProfileUseCase by lazy {
        GetMyProfileUseCase(profileRepository)
    }

    val getMyCoursesUseCase: GetMyCoursesUseCase by lazy {
        GetMyCoursesUseCase(profileRepository)
    }
    
    val getSavedCoursesUseCase: GetSavedCoursesUseCase by lazy {
        GetSavedCoursesUseCase(profileRepository)
    }

    val getCourseDetailUseCase: GetCourseDetailUseCase by lazy {
        GetCourseDetailUseCase(courseRepository)
    }

    val deleteCourseUseCase: DeleteCourseUseCase by lazy {
        DeleteCourseUseCase(courseRepository)
    }
    
    val createCourseUseCase: CreateCourseUseCase by lazy {
        CreateCourseUseCase(courseRepository)
    }
    
    val saveCourseUseCase: SaveCourseUseCase by lazy {
        SaveCourseUseCase(courseRepository)
    }
    
    val updateCourseUseCase: UpdateCourseUseCase by lazy {
        UpdateCourseUseCase(courseRepository)
    }

    val uploadImageUseCase: UploadImageUseCase by lazy {
        UploadImageUseCase(courseRepository)
    }

    val updateProfileUseCase: com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase by lazy {
        com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase(profileRepository)
    }
}