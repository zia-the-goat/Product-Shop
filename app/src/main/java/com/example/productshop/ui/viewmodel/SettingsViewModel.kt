package com.example.productshop.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class BrandTheme {
    BLUE, EMERALD, ROSE
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    )
        private set

    var brandTheme by mutableStateOf(
        BrandTheme.valueOf(prefs.getString("brand_theme", BrandTheme.BLUE.name) ?: BrandTheme.BLUE.name)
    )
        private set

    var notificationsEnabled by mutableStateOf(prefs.getBoolean("notifications_enabled", true))
        private set

    var biometricEnabled by mutableStateOf(prefs.getBoolean("biometric_enabled", true))
        private set

    var language by mutableStateOf(prefs.getString("language", "English") ?: "English")
        private set

    fun setTheme(mode: ThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun updateBrandTheme(theme: BrandTheme) {
        brandTheme = theme
        prefs.edit().putString("brand_theme", theme.name).apply()
    }

    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun toggleBiometric(enabled: Boolean) {
        biometricEnabled = enabled
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun updateLanguage(lang: String) {
        language = lang
        prefs.edit().putString("language", lang).apply()
    }
}
