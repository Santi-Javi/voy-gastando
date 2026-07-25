package com.voygastando.app.domain.usecase

import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.domain.model.ShoppingTotals

class MoneyCalculator {
    fun subtotal(unitPrice: Long, quantity: Int): Long {
        require(unitPrice > 0) { "El importe debe ser mayor a cero." }
        require(quantity in 1..MAX_QUANTITY) { "La cantidad no es válida." }
        return checkedMultiply(unitPrice, quantity.toLong())
    }

    fun validateNewItem(unitPrice: Long, quantity: Int, currentTotal: Long) {
        require(unitPrice in 1..MAX_UNIT_PRICE) { "El importe ingresado es demasiado alto." }
        val subtotal = subtotal(unitPrice, quantity)
        checkedAdd(currentTotal, subtotal)
        require(currentTotal + subtotal <= MAX_TOTAL) { "El total de la compra es demasiado alto." }
    }

    fun totals(session: ShoppingSession?): ShoppingTotals {
        if (session == null) {
            return ShoppingTotals(
                total = 0,
                unitCount = 0,
                recordCount = 0,
                budgetRemaining = null,
                budgetExceeded = null,
                budgetUsagePercent = null,
                averagePerUnit = 0
            )
        }

        val unitCount = session.items.sumOf { it.quantity }
        val budget = session.budget
        val remaining = budget?.let { (it - session.total).coerceAtLeast(0) }
        val exceeded = budget?.let { (session.total - it).coerceAtLeast(0) }?.takeIf { it > 0 }
        val percent = budget?.takeIf { it > 0 }?.let { ((session.total * 100) / it).toInt() }

        return ShoppingTotals(
            total = session.total,
            unitCount = unitCount,
            recordCount = session.items.size,
            budgetRemaining = remaining,
            budgetExceeded = exceeded,
            budgetUsagePercent = percent,
            averagePerUnit = if (unitCount == 0) 0 else session.total / unitCount
        )
    }

    fun sumItems(items: List<ShoppingItem>): Long = items.fold(0L) { total, item ->
        checkedAdd(total, item.subtotal)
    }

    private fun checkedMultiply(left: Long, right: Long): Long {
        val result = Math.multiplyExact(left, right)
        require(result <= MAX_TOTAL) { "El importe ingresado es demasiado alto." }
        return result
    }

    private fun checkedAdd(left: Long, right: Long): Long {
        val result = Math.addExact(left, right)
        require(result <= MAX_TOTAL) { "El total de la compra es demasiado alto." }
        return result
    }

    companion object {
        const val MAX_QUANTITY = 99
        const val MAX_UNIT_PRICE = 999_999_999L
        const val MAX_TOTAL = 99_999_999_999L
    }
}
