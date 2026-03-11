package com.example.cursy.features.chat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cursy.features.chat.data.local.dao.ChatDao
import com.example.cursy.features.chat.data.local.entities.MessageEntity
import com.example.cursy.features.login.Local.Dao.HuellaDao
import com.example.cursy.features.login.Local.Entities.BiometricEntity

@Database(entities = [MessageEntity::class,
    BiometricEntity::class], version = 2, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun huellaDao(): HuellaDao
}
