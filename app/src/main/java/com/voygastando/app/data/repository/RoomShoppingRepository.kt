package com.voygastando.app.data.repository

import com.voygastando.app.data.local.ShoppingDao
import com.voygastando.app.data.local.entity.ShoppingItemEntity
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.repository.SettingsRepository
import com.voygastando.app.domain.repository.ShoppingRepository
import com.voygastando.app.domain.usecase.MoneyCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomShoppingRepository(
    private val dao: ShoppingDao,
    private val settingsRepository: SettingsRepository,
    private val moneyCalculator: MoneyCalculator,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ShoppingRepository {
    override fun observeActiveSession(): Flow<com.voygastando.app.domain.model.ShoppingSession?> =
        dao.observeActiveSession().map { it?.toDomain() }

    override fun observeSession(sessionId: Long): Flow<com.voygastando.app.domain.model.ShoppingSession?> =
        dao.observeSession(sessionId).map { it?.toDomain() }

    override fun observeCompletedSessions(): Flow<List<com.voygastando.app.domain.model.ShoppingSession>> =
        dao.observeCompletedSessions().map { sessions -> sessions.map { it.toDomain() } }

    override suspend fun startShoppingSession(budget: Long?): Long {
        require(budget == null || budget > 0) { "El presupuesto debe ser mayor a cero." }
        val settings = settingsRepository.moneySettings.first()
        return dao.createActiveSession(
            startedAt = clock(),
            budget = budget,
            currencyCode = settings.currencyCode,
            currencySymbol = settings.currencySymbol
        )
    }

    override suspend fun addItem(unitPrice: Long, quantity: Int): ShoppingItem {
        val current = dao.getActiveSession() ?: error("No hay una compra activa.")
        moneyCalculator.validateNewItem(unitPrice, quantity, current.total)
        return dao.addItemToActiveSession(
            unitPrice = unitPrice,
            quantity = quantity,
            createdAt = clock()
        ).toDomain()
    }

    override suspend fun updateItem(itemId: Long, unitPrice: Long, quantity: Int) {
        val item = dao.getItem(itemId) ?: error("No se encontro el producto.")
        val session = dao.getSession(item.sessionId) ?: error("No se encontro la compra.")
        val totalWithoutItem = session.total - item.subtotal
        moneyCalculator.validateNewItem(unitPrice, quantity, totalWithoutItem)
        dao.updateItemAndRecalculate(
            item.copy(
                unitPrice = unitPrice,
                quantity = quantity,
                subtotal = moneyCalculator.subtotal(unitPrice, quantity)
            )
        )
    }

    override suspend fun deleteItem(itemId: Long) {
        val item = dao.getItem(itemId) ?: error("No se encontro el producto.")
        dao.deleteItemAndRecalculate(item)
    }

    override suspend fun undoLastItem(): ShoppingItem? {
        val active = dao.getActiveSession() ?: return null
        val item = dao.getLastItem(active.id) ?: return null
        dao.deleteItemAndRecalculate(item)
        return item.toDomain()
    }

    override suspend fun restoreItem(item: ShoppingItem): ShoppingItem {
        val active = dao.getActiveSession() ?: error("No hay una compra activa.")
        require(item.sessionId == active.id) { "El producto no pertenece a la compra activa." }
        moneyCalculator.validateNewItem(item.unitPrice, item.quantity, active.total)
        val restored = ShoppingItemEntity(
            sessionId = active.id,
            unitPrice = item.unitPrice,
            quantity = item.quantity,
            subtotal = moneyCalculator.subtotal(item.unitPrice, item.quantity),
            createdAt = clock(),
            sortOrder = dao.nextSortOrder(active.id)
        )
        val restoredId = dao.insertItem(restored)
        dao.recalculateSessionTotal(active.id)
        return restored.copy(id = restoredId).toDomain()
    }

    override suspend fun updateBudget(budget: Long?) {
        require(budget == null || budget > 0) { "El presupuesto debe ser mayor a cero." }
        val active = dao.getActiveSession() ?: error("No hay una compra activa.")
        dao.updateBudget(active.id, budget)
    }

    override suspend fun finishActiveSession(): Long = dao.finishActiveSession(clock())

    override suspend fun deleteCompletedSession(sessionId: Long) {
        val session = dao.getSession(sessionId) ?: error("No se encontro la compra.")
        require(session.status == com.voygastando.app.data.local.entity.SessionStatus.COMPLETED) {
            "Solo se pueden eliminar compras finalizadas."
        }
        dao.deleteSessionById(sessionId)
    }
}
