package com.voygastando.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.voygastando.app.data.local.entity.ShoppingItemEntity
import com.voygastando.app.data.local.entity.ShoppingSessionEntity

@Database(
    entities = [
        ShoppingSessionEntity::class,
        ShoppingItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VoyGastandoDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao
}
