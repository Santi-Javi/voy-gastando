package com.voygastando.app

import android.app.Application
import androidx.room.Room
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
    ).build()

    val settingsRepository: SettingsRepository = SettingsDataStoreRepository(application)
    val moneyCalculator = MoneyCalculator()
    val moneyFormatter = MoneyFormatter()
    val shoppingRepository: ShoppingRepository = RoomShoppingRepository(
        dao = database.shoppingDao(),
        settingsRepository = settingsRepository,
        moneyCalculator = moneyCalculator
    )
}
