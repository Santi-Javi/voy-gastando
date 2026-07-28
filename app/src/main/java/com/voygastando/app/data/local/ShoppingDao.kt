package com.voygastando.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.voygastando.app.data.local.entity.SessionStatus
import com.voygastando.app.data.local.entity.ShoppingItemEntity
import com.voygastando.app.data.local.entity.ShoppingSessionEntity
import com.voygastando.app.data.local.entity.ShoppingSessionWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Transaction
    @Query("SELECT * FROM shopping_sessions WHERE status = 'ACTIVE' LIMIT 1")
    fun observeActiveSession(): Flow<ShoppingSessionWithItems?>

    @Transaction
    @Query("SELECT * FROM shopping_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSession(sessionId: Long): Flow<ShoppingSessionWithItems?>

    @Transaction
    @Query("SELECT * FROM shopping_sessions WHERE status = 'COMPLETED' ORDER BY finishedAt DESC, startedAt DESC")
    fun observeCompletedSessions(): Flow<List<ShoppingSessionWithItems>>

    @Query("SELECT * FROM shopping_sessions WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveSession(): ShoppingSessionEntity?

    @Query("SELECT * FROM shopping_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: Long): ShoppingSessionEntity?

    @Query("SELECT * FROM shopping_items WHERE sessionId = :sessionId ORDER BY sortOrder ASC")
    suspend fun getItems(sessionId: Long): List<ShoppingItemEntity>

    @Query("SELECT * FROM shopping_items WHERE id = :itemId LIMIT 1")
    suspend fun getItem(itemId: Long): ShoppingItemEntity?

    @Query("SELECT * FROM shopping_items WHERE sessionId = :sessionId ORDER BY sortOrder DESC LIMIT 1")
    suspend fun getLastItem(sessionId: Long): ShoppingItemEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM shopping_items WHERE sessionId = :sessionId")
    suspend fun nextSortOrder(sessionId: Long): Long

    @Insert
    suspend fun insertSession(session: ShoppingSessionEntity): Long

    @Insert
    suspend fun insertItem(item: ShoppingItemEntity): Long

    @Update
    suspend fun updateSession(session: ShoppingSessionEntity)

    @Update
    suspend fun updateItem(item: ShoppingItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingItemEntity)

    @Query("UPDATE shopping_sessions SET status = :status, finishedAt = :finishedAt WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus, finishedAt: Long?)

    @Query("UPDATE shopping_sessions SET budget = :budget WHERE id = :sessionId")
    suspend fun updateBudget(sessionId: Long, budget: Long?)

    @Query("DELETE FROM shopping_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Transaction
    suspend fun createActiveSession(startedAt: Long, budget: Long?, currencyCode: String, currencySymbol: String): Long {
        val active = getActiveSession()
        if (active != null) return active.id

        return insertSession(
            ShoppingSessionEntity(
                startedAt = startedAt,
                finishedAt = null,
                budget = budget,
                total = 0,
                status = SessionStatus.ACTIVE,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol
            )
        )
    }

    @Transaction
    suspend fun addItemToActiveSession(unitPrice: Long, quantity: Int, createdAt: Long): ShoppingItemEntity {
        val session = requireNotNull(getActiveSession()) { "No hay una compra activa." }
        val subtotal = unitPrice * quantity
        val sortOrder = nextSortOrder(session.id)
        val item = ShoppingItemEntity(
            sessionId = session.id,
            unitPrice = unitPrice,
            quantity = quantity,
            subtotal = subtotal,
            createdAt = createdAt,
            sortOrder = sortOrder
        )
        val itemId = insertItem(item)
        updateSession(session.copy(total = session.total + subtotal))
        return item.copy(id = itemId)
    }

    @Transaction
    suspend fun deleteItemAndRecalculate(item: ShoppingItemEntity) {
        deleteItem(item)
        recalculateSessionTotal(item.sessionId)
    }

    @Transaction
    suspend fun updateItemAndRecalculate(item: ShoppingItemEntity) {
        updateItem(item.copy(subtotal = item.unitPrice * item.quantity))
        recalculateSessionTotal(item.sessionId)
    }

    @Transaction
    suspend fun finishActiveSession(finishedAt: Long): Long {
        val session = requireNotNull(getActiveSession()) { "No hay una compra activa." }
        val items = getItems(session.id)
        require(items.isNotEmpty()) { "La compra todavia no tiene productos." }
        updateSession(
            session.copy(
                finishedAt = finishedAt,
                status = SessionStatus.COMPLETED,
                total = items.sumOf { it.subtotal }
            )
        )
        return session.id
    }

    @Transaction
    suspend fun recalculateSessionTotal(sessionId: Long) {
        val session = getSession(sessionId) ?: return
        val total = getItems(sessionId).sumOf { it.subtotal }
        updateSession(session.copy(total = total))
    }
}
