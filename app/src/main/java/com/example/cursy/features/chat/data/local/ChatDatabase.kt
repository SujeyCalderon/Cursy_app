package com.example.cursy.features.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cursy.features.chat.data.local.dao.ChatDao
import com.example.cursy.features.chat.data.local.entities.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
