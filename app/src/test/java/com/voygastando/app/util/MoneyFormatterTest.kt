package com.voygastando.app.util

import com.voygastando.app.domain.model.MoneySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    private val formatter = MoneyFormatter()

    @Test
    fun format_withoutCents_usesArgentineSeparators() {
        assertEquals("\$ 12.500", formatter.format(12_500L))
    }

    @Test
    fun format_withCents_usesCommaDecimalSeparator() {
        val settings = MoneySettings(centsEnabled = true)

        assertEquals("\$ 12.500,50", formatter.format(1_250_050L, settings))
    }

    @Test
    fun format_supportsCustomSymbol() {
        val settings = MoneySettings(currencySymbol = "ARS")

        assertEquals("ARS 1.000", formatter.format(1_000L, settings))
    }
}
