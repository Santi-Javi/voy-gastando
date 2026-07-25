package com.voygastando.app.domain.usecase

import com.voygastando.app.data.local.entity.SessionStatus
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyCalculatorTest {
    private val calculator = MoneyCalculator()

    @Test
    fun subtotal_multipliesUnitPriceByQuantity() {
        assertEquals(3_750L, calculator.subtotal(1_250L, 3))
    }

    @Test
    fun subtotal_rejectsZeroAmount() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.subtotal(0L, 1)
        }
    }

    @Test
    fun subtotal_rejectsInvalidQuantity() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.subtotal(1_000L, 0)
        }
    }

    @Test
    fun totals_calculatesBudgetRemaining() {
        val session = session(
            budget = 100_000L,
            items = listOf(
                item(unitPrice = 10_000L),
                item(unitPrice = 5_500L)
            )
        )

        val totals = calculator.totals(session)

        assertEquals(15_500L, totals.total)
        assertEquals(2, totals.unitCount)
        assertEquals(84_500L, totals.budgetRemaining)
        assertEquals(null, totals.budgetExceeded)
        assertEquals(15, totals.budgetUsagePercent)
    }

    @Test
    fun totals_calculatesBudgetExceeded() {
        val session = session(
            budget = 10_000L,
            items = listOf(item(unitPrice = 12_000L))
        )

        val totals = calculator.totals(session)

        assertEquals(12_000L, totals.total)
        assertEquals(0L, totals.budgetRemaining)
        assertEquals(2_000L, totals.budgetExceeded)
        assertEquals(120, totals.budgetUsagePercent)
    }

    @Test
    fun totals_calculatesAveragePerUnit() {
        val session = session(
            items = listOf(
                item(unitPrice = 1_000L, quantity = 2),
                item(unitPrice = 3_000L, quantity = 1)
            )
        )

        assertEquals(1_666L, calculator.totals(session).averagePerUnit)
    }

    @Test
    fun validateNewItem_rejectsOverflow() {
        assertThrows(IllegalArgumentException::class.java) {
            calculator.validateNewItem(
                unitPrice = MoneyCalculator.MAX_UNIT_PRICE,
                quantity = MoneyCalculator.MAX_QUANTITY,
                currentTotal = MoneyCalculator.MAX_TOTAL
            )
        }
    }

    private fun session(
        budget: Long? = null,
        items: List<ShoppingItem> = emptyList()
    ): ShoppingSession {
        return ShoppingSession(
            id = 1,
            startedAt = 1_700_000_000_000L,
            finishedAt = null,
            budget = budget,
            total = calculator.sumItems(items),
            status = SessionStatus.ACTIVE,
            currencyCode = "ARS",
            currencySymbol = "\$",
            items = items
        )
    }

    private fun item(unitPrice: Long, quantity: Int = 1): ShoppingItem {
        return ShoppingItem(
            id = 1,
            sessionId = 1,
            unitPrice = unitPrice,
            quantity = quantity,
            subtotal = unitPrice * quantity,
            createdAt = 1_700_000_000_000L,
            sortOrder = 1
        )
    }
}
