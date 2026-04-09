package com.example.cursy.features.notifications.data.di

import android.content.Context
import androidx.room.Room
import com.example.cursy.features.notifications.data.local.NotificationDao
import com.example.cursy.features.notifications.data.local.NotificationDatabase
import com.example.cursy.features.notifications.data.repositories.NotificationRepositoryImpl
import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import com.example.cursy.features.notifications.domain.usecases.GetNotificationsUseCase
import com.example.cursy.features.notifications.domain.usecases.InsertNotificationUseCase
import com.example.cursy.features.notifications.domain.usecases.MarkNotificationAsReadUseCase
import com.example.cursy.core.Hardware.Domain.DeviceNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationDatabase(@ApplicationContext context: Context): NotificationDatabase {
        return Room.databaseBuilder(
            context,
            NotificationDatabase::class.java,
            "notification_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNotificationDao(database: NotificationDatabase): NotificationDao {
        return database.dao()
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(dao: NotificationDao): NotificationRepository {
        return NotificationRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(repository: NotificationRepository): GetNotificationsUseCase {
        return GetNotificationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInsertNotificationUseCase(
        repository: NotificationRepository,
        deviceNotifier: DeviceNotifier
    ): InsertNotificationUseCase {
        return InsertNotificationUseCase(repository, deviceNotifier)
    }

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(repository: NotificationRepository): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(repository)
    }
}
