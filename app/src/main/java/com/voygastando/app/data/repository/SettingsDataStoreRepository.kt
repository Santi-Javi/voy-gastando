package com.voygastando.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStoreRepository(
    context: Context
) : SettingsRepository {
    private val dataStore = context.settingsDataStore

    override val moneySettings: Flow<MoneySettings> = dataStore.data.map { preferences ->
        MoneySettings(
            currencyCode = "ARS",
            currencySymbol = preferences[CURRENCY_SYMBOL] ?: "\$",
            centsEnabled = preferences[CENTS_ENABLED] ?: false
        )
    }

    override suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL] = symbol.ifBlank { "\$" }
        }
    }

    override suspend fun setCentsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CENTS_ENABLED] = enabled
        }
    }

    private companion object {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CENTS_ENABLED = booleanPreferencesKey("cents_enabled")
    }
}
