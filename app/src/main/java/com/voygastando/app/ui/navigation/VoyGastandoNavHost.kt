package com.voygastando.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseScreen
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseUiState
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseViewModel
import com.voygastando.app.ui.screen.home.HomeScreen
import com.voygastando.app.util.MoneyFormatter

@Composable
fun VoyGastandoNavHost(
    uiState: ActivePurchaseUiState,
    viewModel: ActivePurchaseViewModel,
    moneyFormatter: MoneyFormatter,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    LaunchedEffect(uiState.isLoading, uiState.hasActiveSession) {
        if (!uiState.isLoading && uiState.hasActiveSession) {
            navController.navigate(AppRoute.ActivePurchase.route) {
                popUpTo(AppRoute.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier
    ) {
        composable(AppRoute.Home.route) {
            HomeScreen(
                onStartShopping = { budget -> viewModel.startSession(budget) }
            )
        }
        composable(AppRoute.ActivePurchase.route) {
            ActivePurchaseScreen(
                uiState = uiState,
                formatter = moneyFormatter,
                onAddDemoItem = { viewModel.addItem(1_000L) },
                onErrorShown = viewModel::clearError
            )
        }
    }
}
