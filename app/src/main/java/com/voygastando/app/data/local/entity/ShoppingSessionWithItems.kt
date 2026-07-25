package com.voygastando.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingSessionWithItems(
    @Embedded val session: ShoppingSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val items: List<ShoppingItemEntity>
)
