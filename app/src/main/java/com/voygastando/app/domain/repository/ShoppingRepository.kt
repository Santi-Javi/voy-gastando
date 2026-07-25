package com.voygastando.app.domain.repository

import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeActiveSession(): Flow<ShoppingSession?>

    suspend fun startShoppingSession(budget: Long?): Long

    suspend fun addItem(unitPrice: Long, quantity: Int): ShoppingItem

    suspend fun undoLastItem(): ShoppingItem?

    suspend fun restoreItem(item: ShoppingItem): ShoppingItem

    suspend fun updateBudget(budget: Long?)
}
