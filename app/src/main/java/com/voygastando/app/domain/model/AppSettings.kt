package com.voygastando.app.domain.model

data class AppSettings(
    val moneySettings: MoneySettings = MoneySettings(),
    val vibrateOnAdd: Boolean = true,
    val soundOnAdd: Boolean = false,
    val keepScreenOnDuringPurchase: Boolean = false,
    val hideAmountsOnLockScreen: Boolean = true,
    val confirmBeforeFinish: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)
