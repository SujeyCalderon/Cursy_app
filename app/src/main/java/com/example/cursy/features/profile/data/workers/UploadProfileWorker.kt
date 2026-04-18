package com.example.cursy.features.profile.data.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.profile.data.local.ProfileNotificationManager
import com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@HiltWorker
class UploadProfileWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploadImageUseCase: UploadImageUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = ProfileNotificationManager(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val imagePath = inputData.getString(KEY_IMAGE_PATH)
        val name = inputData.getString(KEY_NAME)
        val bio = inputData.getString(KEY_BIO)
        val university = inputData.getString(KEY_UNIVERSITY)

        Log.d(TAG, "Iniciando trabajo. Imagen: $imagePath")
        notificationManager.showUploadingNotification()

        return@withContext try {
            var uploadedUrl: String? = null

            if (imagePath != null) {
                var file = File(imagePath)
                if (!file.exists()) {
                    notificationManager.dismissNotification()
                    return@withContext Result.failure(workDataOf(KEY_ERROR to "Archivo no encontrado"))
                }

                //
                file = compressImage(file)

                Log.d(TAG, "Subiendo imagen optimizada: ${file.length()} bytes")
                val uploadResult = uploadImageUseCase(file)

                if (uploadResult.isSuccess) {
                    uploadedUrl = uploadResult.getOrNull()
                } else {
                    val exception = uploadResult.exceptionOrNull()
                    if (shouldRetry(exception)) {
                        return@withContext Result.retry()
                    } else {
                        notificationManager.showErrorNotification("Error al subir imagen")
                        return@withContext Result.failure(workDataOf(KEY_ERROR to (exception?.message ?: "Error de subida")))
                    }
                }
            }

            val updateResult = updateProfileUseCase(
                name = name,
                profileImage = uploadedUrl,
                bio = bio,
                university = university
            )

            if (updateResult.isSuccess) {
                notificationManager.showSuccessNotification()
                Result.success(if (uploadedUrl != null) workDataOf(KEY_RESULT_URL to uploadedUrl) else workDataOf())
            } else {
                val exception = updateResult.exceptionOrNull()
                if (shouldRetry(exception)) {
                    Result.retry()
                } else {
                    notificationManager.showErrorNotification("Error al actualizar perfil")
                    Result.failure(workDataOf(KEY_ERROR to (exception?.message ?: "Error al actualizar perfil")))
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}", e)
            if (shouldRetry(e)) {
                Result.retry()
            } else {
                notificationManager.showErrorNotification("Error inesperado")
                Result.failure(workDataOf(KEY_ERROR to e.message))
            }
        }
    }

    private fun compressImage(file: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val compressedFile = File(context.cacheDir, "temp_profile_comp.jpg")
            val out = FileOutputStream(compressedFile)
            
            // reducir peso
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            out.flush()
            out.close()
            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Error comprimiendo: ${e.message}")
            file // origen
        }
    }

    private fun shouldRetry(exception: Throwable?): Boolean {
        return when (exception) {
            is UnknownHostException,
            is SocketTimeoutException -> true
            else -> false
        }
    }

    companion object {
        const val KEY_IMAGE_PATH = "image_path"
        const val KEY_NAME = "name"
        const val KEY_BIO = "bio"
        const val KEY_UNIVERSITY = "university"
        const val KEY_RESULT_URL = "result_url"
        const val KEY_ERROR = "error"
        private const val TAG = "UploadProfileWorker"
    }
}
