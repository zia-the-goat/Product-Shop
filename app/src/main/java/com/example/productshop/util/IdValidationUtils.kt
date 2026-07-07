package com.example.productshop.util

import java.text.SimpleDateFormat
import java.util.Locale

object IdValidationUtils {

    /**
     * Validates a South African ID number (YYMMDDSSSSCAZ).
     */
    fun isValidSouthAfricanId(id: String): Boolean {
        // 1. Length check
        if (id.length != 13 || !id.all { it.isDigit() }) return false

        // 2. Date of Birth check (YYMMDD)
        val dob = id.substring(0, 6)
        if (!isValidDate(dob)) return false

        // 3. Citizenship check (Digit 11)
        val citizenship = id[10].toString().toInt()
        if (citizenship != 0 && citizenship != 1) return false

        // 4. Luhn Checksum check (Digit 13)
        return isValidLuhn(id)
    }

    private fun isValidDate(dob: String): Boolean {
        val year = dob.substring(0, 2).toInt()
        val month = dob.substring(2, 4).toInt()
        val day = dob.substring(4, 6).toInt()

        if (month !in 1..12) return false
        
        val maxDays = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 0
        }
        
        return day in 1..maxDays
    }

    private fun isLeapYear(year: Int): Boolean {
        // Since we only have 2 digits, we assume 19xx or 20xx.
        // Simplified leap year check for 2-digit year
        val fullYear = if (year < 50) 2000 + year else 1900 + year
        return (fullYear % 4 == 0 && fullYear % 100 != 0) || (fullYear % 400 == 0)
    }

    private fun isValidLuhn(number: String): Boolean {
        var sum = 0
        var alternate = false
        
        for (i in number.length - 1 downTo 0) {
            var n = Character.getNumericValue(number[i])
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n -= 9
                }
            }
            sum += n
            alternate = !alternate
        }
        
        return sum % 10 == 0
    }

    /**
     * Generates a random valid South African ID number.
     */
    fun generateRandomId(): String {
        val random = java.util.Random()
        
        // YYMMDD
        val year = random.nextInt(100)
        val month = random.nextInt(12) + 1
        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }
        val day = random.nextInt(maxDay) + 1
        
        // SSSS (0000-9999)
        val sequence = random.nextInt(10000)
        
        // C (0 or 1)
        val citizenship = random.nextInt(2)
        
        // A (8 or 9)
        val a = if (random.nextBoolean()) 8 else 9
        
        val base = String.format(Locale.US, "%02d%02d%02d%04d%d%d", year, month, day, sequence, citizenship, a)
        
        // Calculate Z (Checksum)
        var sum = 0
        var alternate = true // Start with true because Z is at the end (index 12)
        
        for (i in base.length - 1 downTo 0) {
            var n = Character.getNumericValue(base[i])
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        
        val z = (10 - (sum % 10)) % 10
        
        return base + z
    }
}
