package com.voygastando.app.ui.screen.activepurchase

import com.voygastando.app.domain.model.AppSettings
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.domain.model.ShoppingTotals

data class ActivePurchaseUiState(
    val isLoading: Boolean = true,
    val activeSession: ShoppingSession? = null,
    val totals: ShoppingTotals = ShoppingTotals(
        total = 0,
        unitCount = 0,
        recordCount = 0,
        budgetRemaining = null,
        budgetExceeded = null,
        budgetUsagePercent = null,
        averagePerUnit = 0
    ),
    val moneySettings: MoneySettings = MoneySettings(),
    val appSettings: AppSettings = AppSettings(),
    val currentInput: String = "",
    val currentProductName: String = "",
    val currentAmount: Long = 0,
    val isAdding: Boolean = false,
    val errorMessage: String? = null
) {
    val hasActiveSession: Boolean = activeSession != null
    val lastItem = activeSession?.items?.maxByOrNull { it.sortOrder }
}
