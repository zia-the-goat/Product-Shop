package com.example.productshop.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecurityManager(context: Context) {
    private val gson = Gson()

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    fun getCredentials(): Pair<String?, String?> {
        val email = sharedPreferences.getString("email", null)
        val password = sharedPreferences.getString("password", null)
        return Pair(email, password)
    }

    fun clearCredentials() {
        sharedPreferences.edit().apply {
            remove("email")
            remove("password")
            apply()
        }
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    fun hasStoredCredentials(): Boolean {
        return sharedPreferences.contains("email") && sharedPreferences.contains("password")
    }

    fun saveFaceEmbedding(embedding: FloatArray) {
        val json = gson.toJson(embedding)
        sharedPreferences.edit().putString("face_embedding", json).apply()
    }

    fun getFaceEmbedding(): FloatArray? {
        val json = sharedPreferences.getString("face_embedding", null) ?: return null
        val type = object : TypeToken<FloatArray>() {}.type
        return gson.fromJson(json, type)
    }

    fun isFaceAuthSetup(): Boolean {
        return sharedPreferences.contains("face_embedding")
    }

    fun clearFaceEmbedding() {
        sharedPreferences.edit().remove("face_embedding").apply()
    }
}
