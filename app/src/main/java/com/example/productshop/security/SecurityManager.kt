package com.example.productshop.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        createSharedPreferences(context)
    } catch (e: Exception) {
        context.deleteSharedPreferences("secure_prefs")
        createSharedPreferences(context)
    }

    private fun createSharedPreferences(context: Context) = EncryptedSharedPreferences.create(
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

    fun saveActiveAccountTypeId(id: Long) {
        sharedPreferences.edit().putLong("active_account_type_id", id).apply()
    }

    fun getActiveAccountTypeId(): Long {
        return sharedPreferences.getLong("active_account_type_id", -1L)
    }

    fun saveSystemPassword(password: String) {
        sharedPreferences.edit().putString("system_password", password).apply()
    }

    fun getSystemPassword(): String? {
        return sharedPreferences.getString("system_password", null)
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
            remove("active_account_type_id")
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
}
