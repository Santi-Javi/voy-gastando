package com.voygastando.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseScreen
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseUiState
import com.voygastando.app.ui.screen.activepurchase.ActivePurchaseViewModel
import com.voygastando.app.ui.screen.history.HistoryScreen
import com.voygastando.app.ui.screen.home.HomeScreen
import com.voygastando.app.ui.screen.itemlist.ShoppingItemsScreen
import com.voygastando.app.ui.screen.settings.SettingsScreen
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
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        AppRoute.Home.route,
        AppRoute.History.route,
        AppRoute.Settings.route,
        AppRoute.Summary.route
    )

    LaunchedEffect(uiState.isLoading, uiState.hasActiveSession) {
        if (!uiState.isLoading && uiState.hasActiveSession && currentRoute == AppRoute.Home.route) {
            navController.navigate(AppRoute.ActivePurchase.route) {
                popUpTo(AppRoute.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    hasActiveSession = uiState.hasActiveSession,
                    onHome = {
                        navController.navigate(AppRoute.Home.route) {
                            launchSingleTop = true
                            popUpTo(AppRoute.Home.route)
                        }
                    },
                    onActive = {
                        navController.navigate(AppRoute.ActivePurchase.route) {
                            launchSingleTop = true
                        }
                    },
                    onHistory = {
                        navController.navigate(AppRoute.History.route) {
                            launchSingleTop = true
                        }
                    },
                    onSettings = {
                        navController.navigate(AppRoute.Settings.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = modifier
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(
                    onStartShopping = { budget -> viewModel.startSession(budget) },
                    onOpenHistory = { navController.navigate(AppRoute.History.route) },
                    modifier = Modifier.padding(scaffoldPadding)
                )
            }

            composable(AppRoute.ActivePurchase.route) {
                ActivePurchaseScreen(
                    uiState = uiState,
                    formatter = moneyFormatter,
                    events = viewModel.uiEvents,
                    onAppendDigit = viewModel::appendDigit,
                    onProductNameChange = viewModel::setCurrentProductName,
                    onVoiceCommand = viewModel::applyVoiceCommand,
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
                    onDelete = viewModel::deleteCompletedSession,
                    modifier = Modifier.padding(scaffoldPadding)
                )
            }

            composable(AppRoute.Settings.route) {
                SettingsScreen(
                    settings = uiState.appSettings,
                    onBack = { navController.popBackStack() },
                    onCurrencySymbolChange = viewModel::setCurrencySymbol,
                    onCentsEnabledChange = viewModel::setCentsEnabled,
                    onVibrateChange = viewModel::setVibrateOnAdd,
                    onSoundChange = viewModel::setSoundOnAdd,
                    onKeepScreenOnChange = viewModel::setKeepScreenOnDuringPurchase,
                    onHideLockAmountsChange = viewModel::setHideAmountsOnLockScreen,
                    onConfirmBeforeFinishChange = viewModel::setConfirmBeforeFinish,
                    onThemeModeChange = viewModel::setThemeMode,
                    modifier = Modifier.padding(scaffoldPadding)
                )
            }

            composable(
                route = AppRoute.Summary.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { summaryEntry ->
                val sessionId = summaryEntry.arguments?.getLong("sessionId") ?: 0L
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
                    },
                    modifier = Modifier.padding(scaffoldPadding)
                )
            }

            composable(
                route = AppRoute.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { detailEntry ->
                val sessionId = detailEntry.arguments?.getLong("sessionId") ?: 0L
                val session by viewModel.observeSession(sessionId).collectAsStateWithLifecycle(initialValue = null)
                ShoppingItemsScreen(
                    session = session,
                    moneySettings = uiState.moneySettings,
                    formatter = moneyFormatter,
                    readOnly = true,
                    onBack = { navController.popBackStack() },
                    onEditItem = { _, _, _, _ -> },
                    onDeleteItem = { }
                )
            }
        }
    }
}
