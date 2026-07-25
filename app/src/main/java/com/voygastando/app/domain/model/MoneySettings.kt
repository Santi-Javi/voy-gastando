package com.voygastando.app.domain.model

data class MoneySettings(
    val currencyCode: String = "ARS",
    val currencySymbol: String = "\$",
    val centsEnabled: Boolean = false
)
