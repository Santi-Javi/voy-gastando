package com.voygastando.app.domain.repository

import com.voygastando.app.domain.model.AppSettings
import com.voygastando.app.domain.model.AppThemeMode
import com.voygastando.app.domain.model.MoneySettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appSettings: Flow<AppSettings>

    val moneySettings: Flow<MoneySettings>

    suspend fun setCurrencySymbol(symbol: String)

    suspend fun setCentsEnabled(enabled: Boolean)

    suspend fun setVibrateOnAdd(enabled: Boolean)

    suspend fun setSoundOnAdd(enabled: Boolean)

    suspend fun setKeepScreenOnDuringPurchase(enabled: Boolean)

    suspend fun setHideAmountsOnLockScreen(enabled: Boolean)

    suspend fun setConfirmBeforeFinish(enabled: Boolean)

    suspend fun setThemeMode(mode: AppThemeMode)
}
