package com.voygastando.app.ui.screen.activepurchase

import com.voygastando.app.domain.model.ShoppingItem

sealed interface ActivePurchaseEvent {
    data class ItemAdded(val subtotal: Long) : ActivePurchaseEvent
    data class ItemUndone(val item: ShoppingItem) : ActivePurchaseEvent
    data class PurchaseFinished(val sessionId: Long) : ActivePurchaseEvent
    data object ItemRestored : ActivePurchaseEvent
    data class Message(val text: String) : ActivePurchaseEvent
}
