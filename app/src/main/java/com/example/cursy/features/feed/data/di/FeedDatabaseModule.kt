package com.example.cursy.features.feed.data.di

import android.content.Context
import androidx.room.Room
import com.example.cursy.features.feed.data.local.FeedDao
import com.example.cursy.features.feed.data.local.FeedDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedDatabaseModule {

    @Provides
    @Singleton
    fun provideFeedDatabase(@ApplicationContext context: Context): FeedDatabase {
        return Room.databaseBuilder(
            context,
            FeedDatabase::class.java,
            "feed_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFeedDao(database: FeedDatabase): FeedDao = database.feedDao()
}
