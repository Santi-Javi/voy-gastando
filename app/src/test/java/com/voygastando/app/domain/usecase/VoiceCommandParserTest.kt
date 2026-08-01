package com.voygastando.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceCommandParserTest {
    private val parser = VoiceCommandParser()

    @Test
    fun parsesProductPriceQuantityAndAddCommand() {
        val command = parser.parsePurchase("leche 2500 tres productos sumar")

        assertNotNull(command)
        assertEquals("leche", command?.name)
        assertEquals(2500L, command?.price)
        assertEquals(3, command?.quantity)
        assertTrue(command?.shouldAdd == true)
        assertTrue(command?.shouldSubtract == false)
    }

    @Test
    fun parsesMultiplierQuantity() {
        val command = parser.parsePurchase("agregar yerba 3500 por 2")

        assertNotNull(command)
        assertEquals("yerba", command?.name)
        assertEquals(3500L, command?.price)
        assertEquals(2, command?.quantity)
        assertTrue(command?.shouldAdd == true)
    }

    @Test
    fun usesOneWhenQuantityIsMissing() {
        val command = parser.parsePurchase("pan 1800 sumar")

        assertNotNull(command)
        assertEquals("pan", command?.name)
        assertEquals(1800L, command?.price)
        assertEquals(1, command?.quantity)
    }

    @Test
    fun parsesSpanishNumberWords() {
        val command = parser.parsePurchase("leche dos mil quinientos tres productos sumar")

        assertNotNull(command)
        assertEquals("leche", command?.name)
        assertEquals(2500L, command?.price)
        assertEquals(3, command?.quantity)
    }

    @Test
    fun parsesSubtractCommandWithoutPrice() {
        val command = parser.parsePurchase("yerba x 1 restar")

        assertNotNull(command)
        assertEquals("yerba", command?.name)
        assertEquals(null, command?.price)
        assertEquals(1, command?.quantity)
        assertTrue(command?.shouldSubtract == true)
    }
}
