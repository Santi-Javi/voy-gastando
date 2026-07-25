package com.voygastando.app.data.repository

import com.voygastando.app.data.local.entity.ShoppingItemEntity
import com.voygastando.app.data.local.entity.ShoppingSessionWithItems
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    sessionId = sessionId,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt,
    sortOrder = sortOrder
)

fun ShoppingItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    sessionId = sessionId,
    unitPrice = unitPrice,
    quantity = quantity,
    subtotal = subtotal,
    createdAt = createdAt,
    sortOrder = sortOrder
)

fun ShoppingSessionWithItems.toDomain(): ShoppingSession = ShoppingSession(
    id = session.id,
    startedAt = session.startedAt,
    finishedAt = session.finishedAt,
    budget = session.budget,
    total = session.total,
    status = session.status,
    currencyCode = session.currencyCode,
    currencySymbol = session.currencySymbol,
    items = items.sortedBy { it.sortOrder }.map { it.toDomain() }
)
