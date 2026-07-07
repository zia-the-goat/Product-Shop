package com.example.productshop

import com.example.productshop.util.IdValidationUtils
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun saIdValidation_isCorrect() {
        // Valid ID (Corrected Luhn checksum from user example)
        assertTrue(IdValidationUtils.isValidSouthAfricanId("9202205000089"))

        // Test random ID generator
        repeat(100) {
            val randomId = IdValidationUtils.generateRandomId()
            assertTrue("Failed for $randomId", IdValidationUtils.isValidSouthAfricanId(randomId))
        }

        // Another valid ID
        assertTrue(IdValidationUtils.isValidSouthAfricanId("8001015009087"))
        assertFalse(IdValidationUtils.isValidSouthAfricanId("920220500008"))
        assertFalse(IdValidationUtils.isValidSouthAfricanId("92022050000877"))

        // Invalid Date (Month 13)
        assertFalse(IdValidationUtils.isValidSouthAfricanId("9213205000087"))

        // Invalid Date (Day 32)
        assertFalse(IdValidationUtils.isValidSouthAfricanId("9202325000087"))

        // Invalid Citizenship (Digit 11 is 2)
        assertFalse(IdValidationUtils.isValidSouthAfricanId("9202205000287"))

        // Invalid Luhn Checksum
        assertFalse(IdValidationUtils.isValidSouthAfricanId("9202205000088"))
    }
}
