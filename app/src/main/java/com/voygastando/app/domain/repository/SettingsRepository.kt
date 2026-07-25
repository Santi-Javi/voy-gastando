package com.voygastando.app.domain.repository

import com.voygastando.app.domain.model.MoneySettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val moneySettings: Flow<MoneySettings>

    suspend fun setCurrencySymbol(symbol: String)

    suspend fun setCentsEnabled(enabled: Boolean)
}
