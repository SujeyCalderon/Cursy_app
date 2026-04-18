package com.example.cursy.core.Hardware.Data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.cursy.core.Hardware.Domain.BiometricManager
import com.example.cursy.features.login.Local.Dao.HuellaDao
import com.example.cursy.features.login.Local.Mapper.toDomain
import com.example.cursy.features.login.Local.Mapper.toEntity
import com.example.cursy.features.profile.domain.entities.Biometric
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class AndroidBiometricManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val huellaDao: HuellaDao
) : BiometricManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun isBiometricAvailable(): Boolean {
        val biometricManager = androidx.biometric.BiometricManager.from(context)
        val result = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        Log.d("BiometricManager", "isBiometricAvailable result: $result")
        return result == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun generateKey(keyAlias: String): Boolean {
        return try {
            if (keyStore.containsAlias(keyAlias)) {
                Log.d("BiometricManager", "Key ya existe: $keyAlias")
                return true
            }
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .build()
            )
            keyGenerator.generateKey()
            Log.d("BiometricManager", "Key generada exitosamente: $keyAlias")
            true
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error generando key: ${e.message}", e)
            false
        }
    }

    override fun getCipherForEncryption(keyAlias: String): Cipher? {
        return try {
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            Log.d("BiometricManager", "Cipher para cifrado listo")
            cipher
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error preparando cipher: ${e.message}", e)
            null
        }
    }

    override fun encryptWithCipher(cipher: Cipher, token: String): String? {
        return try {
            val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)
            val encryptedToken = Base64.encodeToString(encrypted, Base64.DEFAULT)
            val result = "$iv:$encryptedToken"
            Log.d("BiometricManager", "Token cifrado exitosamente")
            result
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error cifrando token: ${e.message}", e)
            null
        }
    }

    override fun getCipherForDecryption(keyAlias: String, iv: ByteArray): Cipher? {
        return try {
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            Log.d("BiometricManager", "Cipher para descifrado listo")
            cipher
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error preparando cipher para descifrado: ${e.message}", e)
            null
        }
    }

    override fun decryptWithCipher(cipher: Cipher, encryptedToken: String): String? {
        return try {
            val data = Base64.decode(encryptedToken, Base64.DEFAULT)
            val decrypted = cipher.doFinal(data)
            val result = String(decrypted, Charsets.UTF_8)
            Log.d("BiometricManager", "Token descifrado exitosamente con cipher")
            result
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error descifrando token con cipher: ${e.message}", e)
            null
        }
    }

    override fun decryptToken(keyAlias: String, encryptedData: String): String? {
        return try {
            val parts = encryptedData.split(":")
            if (parts.size != 2) {
                Log.e("BiometricManager", "Formato de token cifrado inválido")
                return null
            }
            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val encryptedToken = Base64.decode(parts[1], Base64.DEFAULT)
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val result = String(cipher.doFinal(encryptedToken), Charsets.UTF_8)
            Log.d("BiometricManager", "Token descifrado exitosamente")
            result
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error descifrando token: ${e.message}", e)
            null
        }
    }

    override suspend fun saveHuella(biometric: Biometric): Result<Unit> {
        return try {
            huellaDao.insertHuella(biometric.toEntity())
            Log.d("BiometricManager", "Huella guardada para userId: ${biometric.userId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error guardando huella: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getHuella(userId: String): Result<Biometric?> {
        return try {
            val result = huellaDao.getHuellaByUserId(userId)?.toDomain()
            Log.d("BiometricManager", "getHuella userId: $userId -> encontrado: ${result != null}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error obteniendo huella: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun getAllHuellas(): Result<List<Biometric>> {
        return try {
            val result = huellaDao.getAllHuellas().map { it.toDomain() }
            Log.d("BiometricManager", "getAllHuellas: ${result.size} encontradas")
            Result.success(result)
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error obteniendo todas las huellas: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteHuella(userId: String): Result<Unit> {
        return try {
            huellaDao.deleteHuella(userId)
            Log.d("BiometricManager", "Huella eliminada para userId: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BiometricManager", "Error eliminando huella: ${e.message}", e)
            Result.failure(e)
        }
    }
}