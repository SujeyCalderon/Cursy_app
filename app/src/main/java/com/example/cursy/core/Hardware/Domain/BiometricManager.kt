package com.example.cursy.core.Hardware.Domain

import com.example.cursy.features.profile.domain.entities.Biometric
import javax.crypto.Cipher

interface BiometricManager {
    fun isBiometricAvailable(): Boolean
    fun generateKey(keyAlias: String): Boolean
    fun getCipherForEncryption(keyAlias: String): Cipher?
    fun encryptWithCipher(cipher: Cipher, token: String): String?
    fun getCipherForDecryption(keyAlias: String, iv: ByteArray): Cipher?
    fun decryptWithCipher(cipher: Cipher, encryptedToken: String): String?
    fun decryptToken(keyAlias: String, encryptedToken: String): String?
    suspend fun saveHuella(biometric: Biometric): Result<Unit>
    suspend fun getHuella(userId: String): Result<Biometric?>
    suspend fun getAllHuellas(): Result<List<Biometric>>
    suspend fun deleteHuella(userId: String): Result<Unit>
}