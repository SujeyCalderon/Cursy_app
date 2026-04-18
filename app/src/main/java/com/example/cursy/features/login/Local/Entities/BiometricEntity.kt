package com.example.cursy.features.login.Local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BiometricAuth")
data class BiometricEntity (
    @PrimaryKey val userId: String,
    val keyUser: String,
    val tokenLogin: String,
    val stateHuella: Boolean
)