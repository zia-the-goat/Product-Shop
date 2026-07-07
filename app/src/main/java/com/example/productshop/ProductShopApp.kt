package com.example.productshop

import android.app.Application
import com.example.productshop.util.AnalyticsManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.util.OtpManager
import java.io.File

class ProductShopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsManager.initialize(this)
        initializeEnv()
        migrateSystemPassword()
    }

    private fun migrateSystemPassword() {
        val securityManager = SecurityManager(this)
        // If not already in encrypted storage, try to get from .env
        if (securityManager.getSystemPassword() == null) {
            val password = OtpManager.getSystemPassword(this)
            if (password != null) {
                securityManager.saveSystemPassword(password)
            }
        }
    }

    private fun initializeEnv() {
        val envFile = File(filesDir, ".env")
        if (!envFile.exists()) {
            try {
                // Copy from assets on first run
                assets.open(".env").use { input ->
                    envFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                // Fallback if asset missing
                try {
                    envFile.writeText("SYSTEM_PASSWORD=Admin@System2024\n")
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        } else {
            // Ensure SYSTEM_PASSWORD is there even if file exists (e.g. from OTP updates)
            try {
                val lines = envFile.readLines().toMutableList()
                if (lines.none { it.trim().startsWith("SYSTEM_PASSWORD=") }) {
                    lines.add("SYSTEM_PASSWORD=Admin@System2024")
                    envFile.writeText(lines.joinToString("\n") + "\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
