package com.voygastando.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseScreen
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseUiState
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseViewModel
import com.voygastando.app.ui.screen.history.HistoryScreen
import com.voygastando.app.ui.screen.home.HomeScreen
import com.voygastando.app.ui.screen.itemlist.ShoppingItemsScreen
import com.voygastando.app.ui.screen.summary.SummaryScreen
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
                onStartShopping = { budget -> viewModel.startSession(budget) },
                onOpenHistory = { navController.navigate(AppRoute.History.route) }
            )
        }
        composable(AppRoute.ActivePurchase.route) {
            ActivePurchaseScreen(
                uiState = uiState,
                formatter = moneyFormatter,
                events = viewModel.uiEvents,
                onAppendDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
                onClearInput = viewModel::clearInput,
                onAddCurrentInput = viewModel::addCurrentInput,
                onUndoLastItem = viewModel::undoLastItem,
                onRestoreItem = viewModel::restoreItem,
                onUpdateBudget = viewModel::updateBudget,
                onViewItems = { navController.navigate(AppRoute.ActiveItems.route) },
                onFinishPurchase = viewModel::finishActiveSession,
                onPurchaseFinished = { sessionId ->
                    navController.navigate(AppRoute.Summary.create(sessionId)) {
                        popUpTo(AppRoute.ActivePurchase.route) { inclusive = true }
                    }
                },
                onErrorShown = viewModel::clearError
            )
        }
        composable(AppRoute.ActiveItems.route) {
            ShoppingItemsScreen(
                session = uiState.activeSession,
                moneySettings = uiState.moneySettings,
                formatter = moneyFormatter,
                readOnly = false,
                onBack = { navController.popBackStack() },
                onEditItem = viewModel::updateItem,
                onDeleteItem = viewModel::deleteItem
            )
        }
        composable(AppRoute.History.route) {
            val sessions by viewModel.completedSessions.collectAsStateWithLifecycle()
            HistoryScreen(
                sessions = sessions,
                moneySettings = uiState.moneySettings,
                formatter = moneyFormatter,
                onBack = { navController.popBackStack() },
                onOpenSummary = { sessionId -> navController.navigate(AppRoute.Summary.create(sessionId)) },
                onDelete = viewModel::deleteCompletedSession
            )
        }
        composable(
            route = AppRoute.Summary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val session by viewModel.observeSession(sessionId).collectAsStateWithLifecycle(initialValue = null)
            SummaryScreen(
                session = session,
                moneySettings = uiState.moneySettings,
                formatter = moneyFormatter,
                onNewPurchase = {
                    viewModel.startSession(null)
                    navController.navigate(AppRoute.ActivePurchase.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = true }
                    }
                },
                onViewDetail = { id -> navController.navigate(AppRoute.SessionDetail.create(id)) },
                onBackHome = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(0)
                    }
                }
            )
        }
        composable(
            route = AppRoute.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            val session by viewModel.observeSession(sessionId).collectAsStateWithLifecycle(initialValue = null)
            ShoppingItemsScreen(
                session = session,
                moneySettings = uiState.moneySettings,
                formatter = moneyFormatter,
                readOnly = true,
                onBack = { navController.popBackStack() },
                onEditItem = { _, _, _ -> },
                onDeleteItem = { }
            )
        }
    }
}
