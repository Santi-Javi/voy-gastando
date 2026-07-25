package com.voygastando.app.util

import com.voygastando.app.domain.model.MoneySettings
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.absoluteValue

class MoneyFormatter {
    private val symbols = DecimalFormatSymbols(Locale("es", "AR")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    fun format(amount: Long, settings: MoneySettings = MoneySettings()): String {
        val sign = if (amount < 0) "-" else ""
        val absolute = amount.absoluteValue

        return if (settings.centsEnabled) {
            val pesos = absolute / 100
            val cents = absolute % 100
            "$sign${settings.currencySymbol} ${integerFormat().format(pesos)},${cents.toString().padStart(2, '0')}"
        } else {
            "$sign${settings.currencySymbol} ${integerFormat().format(absolute)}"
        }
    }

    fun forTalkBack(amount: Long, settings: MoneySettings = MoneySettings()): String {
        val formatted = format(amount, settings)
        return if (settings.currencyCode == "ARS") "$formatted pesos argentinos" else formatted
    }

    private fun integerFormat(): DecimalFormat = DecimalFormat("#,##0", symbols)
}
