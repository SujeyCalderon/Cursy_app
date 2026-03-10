package com.example.cursy.core.Hardware.Domain

import java.io.File

interface CameraManager {
    fun hasCamera(): Boolean
    fun hasFlash(): Boolean
    suspend fun capturePhoto(): Result<File>
}
