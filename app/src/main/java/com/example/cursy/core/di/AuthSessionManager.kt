package com.example.cursy.core.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor() {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthSessionEntryPoint {
    fun authSessionManager(): AuthSessionManager
}
