package com.voygastando.app.ui.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object ActivePurchase : AppRoute("active_purchase")
}
