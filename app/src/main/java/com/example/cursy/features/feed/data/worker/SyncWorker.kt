package com.example.cursy.features.feed.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.cursy.features.feed.data.local.FeedDao
import com.example.cursy.features.feed.domain.SyncManager
import com.example.cursy.features.feed.domain.repository.FeedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FeedRepository,
    private val feedDao: FeedDao,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Obtener el conteo actual antes de sincronizar
            val currentCount = feedDao.getAllOrdered().size

            // Refrescar el feed (esto actualiza la DB automáticamente según el Repository)
            val result = repository.getFeed()

            if (result.isSuccess) {
                // Obtener el nuevo conteo
                val newItems = feedDao.getAllOrdered().size
                val diff = newItems - currentCount
                
                if (diff > 0) {
                    // Acumular el conteo de nuevos posts si ya había algunos sin ver
                    val existingNew = syncManager.getNewPostsCount()
                    syncManager.setNewPostsCount(existingNew + diff)
                }
                
                syncManager.setLastSyncTime(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
