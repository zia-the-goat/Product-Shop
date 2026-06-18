package com.example.productshop.util

import java.io.File

object OtpManager {
    fun generateOtp(): String {
        return "000000"//(100000..999999).random().toString()
    }

    fun saveOtpToEnv(otp: String) {
        val envFilePath = "C:/Users/ziauddeen.mohamad/.ssh/Git/ProductShop/app/src/main/java/com/example/productshop/.env"
        val envFile = File(envFilePath)
        try {
            val lines = if (envFile.exists()) {
                envFile.readLines().toMutableList()
            } else {
                mutableListOf()
            }
            
            // Log for debugging
            println("Updating .env file at: $envFilePath")
            println("Current lines: ${lines.size}")

            // Remove existing OTP line if any
            lines.removeIf { it.trim().startsWith("OTP=") }
            
            // Add new OTP
            lines.add("OTP=$otp")
            
            envFile.writeText(lines.joinToString("\n") + "\n")
            println("Successfully wrote OTP to .env")
        } catch (e: Exception) {
            println("Error writing to .env: ${e.message}")
            e.printStackTrace()
        }
    }
}
