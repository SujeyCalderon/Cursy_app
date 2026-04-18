package com.example.cursy.features.chat.data.di

import android.content.Context
import androidx.room.Room
import com.example.cursy.features.chat.data.local.ChatDatabase
import com.example.cursy.features.chat.data.local.dao.ChatDao
import com.example.cursy.features.login.Local.Dao.HuellaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "cursy_chat_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideChatDao(database: ChatDatabase): ChatDao {
        return database.chatDao()
    }

    @Provides
    fun provideHuellaDao(database: ChatDatabase): HuellaDao {
        return database.huellaDao()
    }
}
