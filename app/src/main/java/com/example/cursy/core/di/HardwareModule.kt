package com.example.cursy.core.di

import com.example.cursy.core.Hardware.Data.AndroidBiometricManager
import com.example.cursy.core.Hardware.Data.AndroidCameraManager
import com.example.cursy.core.Hardware.Domain.BiometricManager
import com.example.cursy.core.Hardware.Domain.CameraManager
import com.example.cursy.core.Hardware.Data.AndroidDeviceNotifier
import com.example.cursy.core.Hardware.Domain.DeviceNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindDeviceNotifier(deviceNotifier: AndroidDeviceNotifier): DeviceNotifier

    @Binds
    @Singleton
    abstract fun bindCameraManager(impl: AndroidCameraManager): CameraManager

    @Binds
    @Singleton
    abstract fun bindBiometricManager(impl: AndroidBiometricManager): BiometricManager
}