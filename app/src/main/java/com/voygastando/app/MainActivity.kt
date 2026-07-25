package com.voygastando.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            VoyGastandoTheme {
                VoyGastandoNavHost(
                    uiState = uiState,
                    viewModel = viewModel,
                    moneyFormatter = container.moneyFormatter
                )
            }
        }
    }
}
