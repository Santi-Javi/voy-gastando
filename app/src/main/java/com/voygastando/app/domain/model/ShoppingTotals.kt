package com.voygastando.app.domain.model

data class ShoppingTotals(
    val total: Long,
    val unitCount: Int,
    val recordCount: Int,
    val budgetRemaining: Long?,
    val budgetExceeded: Long?,
    val budgetUsagePercent: Int?,
    val averagePerUnit: Long
)
