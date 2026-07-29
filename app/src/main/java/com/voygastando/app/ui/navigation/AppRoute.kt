package com.voygastando.app.ui.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object ActivePurchase : AppRoute("active_purchase")
    data object ActiveItems : AppRoute("active_items")
    data object History : AppRoute("history")
    data object Settings : AppRoute("settings")
    data object Summary : AppRoute("summary/{sessionId}") {
        fun create(sessionId: Long) = "summary/$sessionId"
    }
    data object SessionDetail : AppRoute("session_detail/{sessionId}") {
        fun create(sessionId: Long) = "session_detail/$sessionId"
    }
}
