package com.example.cursy.core.Hardware.Data

import android.content.Context
import android.content.pm.PackageManager
import com.example.cursy.core.Hardware.Domain.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.io.File

class AndroidCameraManager @Inject constructor(
    @ApplicationContext private val context: Context
): CameraManager {

    override fun hasCamera(): Boolean = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    override fun hasFlash(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    override suspend fun capturePhoto(): Result<File> {
        return try {
            val file = File.createTempFile("profile_", ".jpg", context.cacheDir)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}