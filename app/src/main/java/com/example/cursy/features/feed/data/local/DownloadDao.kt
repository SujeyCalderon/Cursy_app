package com.example.cursy.features.feed.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// david: Dao para gestionar las operaciones de descarga en la base de datos
@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE courseId = :courseId")
    suspend fun getDownloadById(courseId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Query("UPDATE downloads SET progress = :progress, status = :status WHERE courseId = :courseId")
    suspend fun updateProgress(courseId: String, progress: Int, status: DownloadStatus)

    @Query("DELETE FROM downloads WHERE courseId = :courseId")
    suspend fun deleteDownload(courseId: String)
}
