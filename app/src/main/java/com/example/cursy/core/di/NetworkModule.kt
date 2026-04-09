package com.example.cursy.core.di

import com.example.cursy.core.network.CoursyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun isAuthEndpoint(encodedPath: String): Boolean =
        encodedPath.contains("/auth/login") || encodedPath.contains("/auth/register")

    private fun normalizeBearerToken(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var t = raw.trim()
        if (t.startsWith("Bearer ", ignoreCase = true)) {
            t = t.substring(7).trim()
        }
        return t.takeIf { it.isNotEmpty() }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authManager: AuthManager,
        authSessionManager: AuthSessionManager
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val path = originalRequest.url.encodedPath
            val isAuth = isAuthEndpoint(path)

            val requestBuilder = originalRequest.newBuilder()

            if (!isAuth) {
                normalizeBearerToken(authManager.getAuthToken())?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
            }

            val response = chain.proceed(requestBuilder.build())

            if (response.code == 401 && !isAuth) {
                authManager.clear()
                authSessionManager.notifySessionExpired()
            }

            response
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://52.20.206.74:8080/api/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideCoursyApi(retrofit: Retrofit): CoursyApi {
        return retrofit.create(CoursyApi::class.java)
    }
}
