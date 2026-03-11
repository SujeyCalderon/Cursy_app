package com.example.cursy.core.di

import com.example.cursy.core.Hardware.Data.AndroidCameraManager
import com.example.cursy.core.Hardware.Domain.CameraManager
import com.example.cursy.core.Hardware.Data.AndroidDeviceNotifier
import com.example.cursy.core.Hardware.Domain.DeviceNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    abstract fun bindCameraManager(cameraManager: AndroidCameraManager): CameraManager

    @Binds
    abstract fun bindDeviceNotifier(deviceNotifier: AndroidDeviceNotifier): DeviceNotifier
}