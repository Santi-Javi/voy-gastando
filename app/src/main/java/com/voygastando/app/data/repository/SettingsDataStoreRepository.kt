package com.voygastando.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voygastando.app.domain.model.AppSettings
import com.voygastando.app.domain.model.AppThemeMode
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStoreRepository(
    context: Context
) : SettingsRepository {
    private val dataStore = context.settingsDataStore

    override val appSettings: Flow<AppSettings> = dataStore.data.map { preferences ->
        val moneySettings = MoneySettings(
            currencyCode = "ARS",
            currencySymbol = preferences[CURRENCY_SYMBOL] ?: "\$",
            centsEnabled = preferences[CENTS_ENABLED] ?: false
        )
        AppSettings(
            moneySettings = moneySettings,
            vibrateOnAdd = preferences[VIBRATE_ON_ADD] ?: true,
            soundOnAdd = preferences[SOUND_ON_ADD] ?: false,
            keepScreenOnDuringPurchase = preferences[KEEP_SCREEN_ON] ?: false,
            hideAmountsOnLockScreen = preferences[HIDE_AMOUNTS_ON_LOCK] ?: true,
            confirmBeforeFinish = preferences[CONFIRM_BEFORE_FINISH] ?: true,
            themeMode = preferences[THEME_MODE]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: AppThemeMode.LIGHT
        )
    }

    override val moneySettings: Flow<MoneySettings> = appSettings.map { it.moneySettings }

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

    override suspend fun setVibrateOnAdd(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[VIBRATE_ON_ADD] = enabled }
    }

    override suspend fun setSoundOnAdd(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[SOUND_ON_ADD] = enabled }
    }

    override suspend fun setKeepScreenOnDuringPurchase(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEEP_SCREEN_ON] = enabled }
    }

    override suspend fun setHideAmountsOnLockScreen(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[HIDE_AMOUNTS_ON_LOCK] = enabled }
    }

    override suspend fun setConfirmBeforeFinish(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[CONFIRM_BEFORE_FINISH] = enabled }
    }

    override suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_MODE] = mode.name }
    }

    private companion object {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CENTS_ENABLED = booleanPreferencesKey("cents_enabled")
        val VIBRATE_ON_ADD = booleanPreferencesKey("vibrate_on_add")
        val SOUND_ON_ADD = booleanPreferencesKey("sound_on_add")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val HIDE_AMOUNTS_ON_LOCK = booleanPreferencesKey("hide_amounts_on_lock")
        val CONFIRM_BEFORE_FINISH = booleanPreferencesKey("confirm_before_finish")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
