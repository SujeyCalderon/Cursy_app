package com.example.cursy.features.feed.domain

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    fun setNewPostsCount(count: Int) {
        prefs.edit().putInt("new_posts_count", count).apply()
    }

    fun getNewPostsCount(): Int = prefs.getInt("new_posts_count", 0)

    fun clearNewPostsCount() {
        prefs.edit().putInt("new_posts_count", 0).apply()
    }

    fun setLastSyncTime(timestamp: Long) {
        prefs.edit().putLong("last_sync_time", timestamp).apply()
    }

    fun getLastSyncTime(): Long = prefs.getLong("last_sync_time", 0L)
}
