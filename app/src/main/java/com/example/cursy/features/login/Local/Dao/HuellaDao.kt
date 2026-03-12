package com.example.cursy.features.login.Local.Dao
import com.example.cursy.features.login.Local.Entities.BiometricEntity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HuellaDao {
    @Insert
    suspend fun insertHuella(biometric: BiometricEntity)

    @Update
    suspend fun updateHuella(biometric: BiometricEntity)

    @Query("SELECT * FROM BiometricAuth WHERE userId = :userId LIMIT 1")
    suspend fun getHuellaByUserId(userId: String): BiometricEntity?

    @Query("SELECT * FROM BiometricAuth WHERE stateHuella = 1")
    suspend fun getAllHuellas(): List<BiometricEntity>  // <- nuevo

    @Query("DELETE FROM BiometricAuth WHERE userId = :userId")
    suspend fun deleteHuella(userId: String)
}
