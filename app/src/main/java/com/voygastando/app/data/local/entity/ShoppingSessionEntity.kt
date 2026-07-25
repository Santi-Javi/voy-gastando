package com.voygastando.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_sessions",
    indices = [
        Index(value = ["status"]),
        Index(value = ["startedAt"])
    ]
)
data class ShoppingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long?,
    val budget: Long?,
    val total: Long,
    val status: SessionStatus,
    val currencyCode: String,
    val currencySymbol: String
)
