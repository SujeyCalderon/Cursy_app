package com.example.cursy.features.Review.Data.Di

import com.example.cursy.features.Review.Data.Repository.ReviewRepositoryImpl
import com.example.cursy.features.Review.Domain.Repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReviewRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl
    ): ReviewRepository
}