package com.voygastando.app.domain.model

import com.voygastando.app.data.local.entity.SessionStatus

data class ShoppingSession(
    val id: Long,
    val startedAt: Long,
    val finishedAt: Long?,
    val budget: Long?,
    val total: Long,
    val status: SessionStatus,
    val currencyCode: String,
    val currencySymbol: String,
    val items: List<ShoppingItem>
)
