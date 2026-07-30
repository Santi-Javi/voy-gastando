package com.voygastando.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowInsetsControllerCompat
import com.voygastando.app.domain.model.AppThemeMode
import com.voygastando.app.ui.navigation.VoyGastandoNavHost
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseViewModel
import com.voygastando.app.ui.theme.VoyGastandoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ActivePurchaseViewModel by viewModels {
        val container = (application as VoyGastandoApplication).appContainer
        ActivePurchaseViewModel.Factory(
            shoppingRepository = container.shoppingRepository,
            settingsRepository = container.settingsRepository,
            moneyCalculator = container.moneyCalculator
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as VoyGastandoApplication).appContainer
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (uiState.appSettings.themeMode) {
                AppThemeMode.SYSTEM -> systemDarkTheme
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            DisposableEffect(darkTheme) {
                window.statusBarColor = if (darkTheme) 0xFF0E0B12.toInt() else 0xFFF4F7F5.toInt()
                window.navigationBarColor = if (darkTheme) 0xFF0E0B12.toInt() else 0xFFFFFFFF.toInt()
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
                onDispose { }
            }

            DisposableEffect(uiState.appSettings.keepScreenOnDuringPurchase, uiState.hasActiveSession) {
                val keepScreenOn = uiState.appSettings.keepScreenOnDuringPurchase && uiState.hasActiveSession
                if (keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            VoyGastandoTheme(darkTheme = darkTheme) {
                VoyGastandoNavHost(
                    uiState = uiState,
                    viewModel = viewModel,
                    moneyFormatter = container.moneyFormatter
                )
            }
        }
    }
}
