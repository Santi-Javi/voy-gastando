package com.voygastando.app.domain.model

data class ShoppingItem(
    val id: Long,
    val sessionId: Long,
    val unitPrice: Long,
    val quantity: Int,
    val subtotal: Long,
    val createdAt: Long,
    val sortOrder: Long
)
