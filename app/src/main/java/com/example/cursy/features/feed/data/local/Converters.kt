package com.example.cursy.features.feed.data.local

import androidx.room.TypeConverter

// david: Converters para manejar tipos personalizados en Room
class Converters {
    @TypeConverter
    fun fromDownloadStatus(status: DownloadStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDownloadStatus(status: String): DownloadStatus {
        return DownloadStatus.valueOf(status)
    }
}
