package com.example.productshop.util

import android.content.Context
import java.io.File

object OtpManager {
    fun generateOtp(): String {
        return "000000"//(100000..999999).random().toString()
    }

    fun saveOtpToEnv(context: Context, otp: String) {
        val envFile = File(context.filesDir, ".env")
        try {
            val lines = if (envFile.exists()) {
                envFile.readLines().toMutableList()
            } else {
                mutableListOf()
            }
            
            // Log for debugging
            println("Updating .env file at: ${envFile.absolutePath}")
            println("Current lines: ${lines.size}")

            // Remove existing OTP line if any
            lines.removeIf { it.trim().startsWith("OTP=") }
            
            // Add new OTP
            lines.add("OTP=$otp")
            
            envFile.writeText(lines.joinToString("\n") + "\n")
            println("Successfully wrote OTP to .env: $otp")
        } catch (e: Exception) {
            println("Error writing to .env: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getSystemPassword(context: Context): String? {
        val envFile = File(context.filesDir, ".env")
        return try {
            if (envFile.exists()) {
                envFile.readLines()
                    .find { it.trim().startsWith("SYSTEM_PASSWORD=") }
                    ?.split("=", limit = 2)
                    ?.getOrNull(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
