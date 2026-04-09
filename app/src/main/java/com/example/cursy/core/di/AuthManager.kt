package com.example.cursy.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun setAuthToken(token: String) {
        val normalized = token.trim().let { t ->
            if (t.startsWith("Bearer ", ignoreCase = true)) t.substring(7).trim() else t
        }
        prefs.edit().putString("auth_token", normalized).apply()
    }

    fun getAuthToken(): String? = prefs.getString("auth_token", null)

    fun setCurrentUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getCurrentUserId(): String? = prefs.getString("user_id", null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
