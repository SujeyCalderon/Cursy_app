package com.example.cursy.features.profile.domain.entities

import androidx.room.PrimaryKey

data class Biometric(
    val userId: String,
    val keyUser: String,
    val tokenLogin: String,
    val stateHuella: Boolean
)