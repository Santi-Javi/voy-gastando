package com.voygastando.app.ui.screen.activepurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voygastando.app.domain.repository.SettingsRepository
import com.voygastando.app.domain.repository.ShoppingRepository
import com.voygastando.app.domain.usecase.MoneyCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivePurchaseViewModel(
    private val shoppingRepository: ShoppingRepository,
    settingsRepository: SettingsRepository,
    private val moneyCalculator: MoneyCalculator
) : ViewModel() {
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ActivePurchaseUiState> = combine(
        shoppingRepository.observeActiveSession(),
        settingsRepository.moneySettings,
        errorMessage
    ) { session, settings, error ->
        ActivePurchaseUiState(
            isLoading = false,
            activeSession = session,
            totals = moneyCalculator.totals(session),
            moneySettings = settings,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActivePurchaseUiState()
    )

    fun startSession(budget: Long?) {
        viewModelScope.launch {
            runCatching { shoppingRepository.startShoppingSession(budget) }
                .onFailure { errorMessage.value = it.message ?: "No se pudo iniciar la compra." }
        }
    }

    fun addItem(unitPrice: Long, quantity: Int = 1) {
        viewModelScope.launch {
            runCatching { shoppingRepository.addItem(unitPrice, quantity) }
                .onFailure { errorMessage.value = it.message ?: "No se pudo agregar el producto." }
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    class Factory(
        private val shoppingRepository: ShoppingRepository,
        private val settingsRepository: SettingsRepository,
        private val moneyCalculator: MoneyCalculator
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ActivePurchaseViewModel::class.java)) {
                return ActivePurchaseViewModel(
                    shoppingRepository = shoppingRepository,
                    settingsRepository = settingsRepository,
                    moneyCalculator = moneyCalculator
                ) as T
            }
            throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
