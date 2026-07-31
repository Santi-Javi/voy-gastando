package com.voygastando.app

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.voygastando.app.data.local.VoyGastandoDatabase
import com.voygastando.app.data.repository.RoomShoppingRepository
import com.voygastando.app.data.repository.SettingsDataStoreRepository
import com.voygastando.app.domain.repository.SettingsRepository
import com.voygastando.app.domain.repository.ShoppingRepository
import com.voygastando.app.domain.usecase.MoneyCalculator
import com.voygastando.app.util.MoneyFormatter

class VoyGastandoApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database = Room.databaseBuilder(
        application,
        VoyGastandoDatabase::class.java,
        "voy_gastando.db"
    ).addMigrations(MIGRATION_1_2).build()

    val settingsRepository: SettingsRepository = SettingsDataStoreRepository(application)
    val moneyCalculator = MoneyCalculator()
    val moneyFormatter = MoneyFormatter()
    val shoppingRepository: ShoppingRepository = RoomShoppingRepository(
        dao = database.shoppingDao(),
        settingsRepository = settingsRepository,
        moneyCalculator = moneyCalculator
    )

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE shopping_items ADD COLUMN name TEXT")
            }
        }
    }
}
