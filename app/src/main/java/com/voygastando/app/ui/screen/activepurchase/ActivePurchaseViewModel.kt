package com.voygastando.app.ui.screen.activepurchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.domain.model.AppThemeMode
import com.voygastando.app.domain.repository.SettingsRepository
import com.voygastando.app.domain.repository.ShoppingRepository
import com.voygastando.app.domain.usecase.MoneyCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivePurchaseViewModel(
    private val shoppingRepository: ShoppingRepository,
    private val settingsRepository: SettingsRepository,
    private val moneyCalculator: MoneyCalculator
) : ViewModel() {
    private val errorMessage = MutableStateFlow<String?>(null)
    private val currentInput = MutableStateFlow("")
    private val currentProductName = MutableStateFlow("")
    private val isAdding = MutableStateFlow(false)
    private val events = MutableSharedFlow<ActivePurchaseEvent>()
    private var lastAddAttemptAt = 0L
    private var lastAddSignature: Pair<Long, Int>? = null

    val uiEvents: SharedFlow<ActivePurchaseEvent> = events

    val completedSessions: StateFlow<List<ShoppingSession>> = shoppingRepository.observeCompletedSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val uiState: StateFlow<ActivePurchaseUiState> = combine(
        shoppingRepository.observeActiveSession(),
        settingsRepository.appSettings,
        combine(currentInput, currentProductName) { input, productName -> input to productName },
        isAdding,
        errorMessage
    ) { session, appSettings, inputAndName, adding, error ->
        val (input, productName) = inputAndName
        val amount = input.toLongOrNull() ?: 0L
        ActivePurchaseUiState(
            isLoading = false,
            activeSession = session,
            totals = moneyCalculator.totals(session),
            moneySettings = appSettings.moneySettings,
            appSettings = appSettings,
            currentInput = input,
            currentProductName = productName,
            currentAmount = amount,
            isAdding = adding,
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
            if (isAdding.value) return@launch
            val now = System.currentTimeMillis()
            val signature = unitPrice to quantity
            if (signature == lastAddSignature && now - lastAddAttemptAt < ADD_DEBOUNCE_MS) {
                return@launch
            }
            lastAddAttemptAt = now
            lastAddSignature = signature

            isAdding.value = true
            runCatching { shoppingRepository.addItem(unitPrice, quantity, currentProductName.value) }
                .onSuccess { item ->
                    currentInput.value = ""
                    currentProductName.value = ""
                    events.emit(ActivePurchaseEvent.ItemAdded(item.subtotal))
                }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo agregar el producto.")) }
            isAdding.value = false
        }
    }

    fun appendDigit(value: String) {
        val next = (currentInput.value + value).trimStart('0')
        currentInput.value = next.take(MAX_INPUT_DIGITS)
    }

    fun backspace() {
        currentInput.value = currentInput.value.dropLast(1)
    }

    fun clearInput() {
        currentInput.value = ""
    }

    fun setCurrentProductName(name: String) {
        currentProductName.value = name.take(MAX_PRODUCT_NAME_LENGTH)
    }

    fun addCurrentInput(quantity: Int = 1) {
        val amount = currentInput.value.toLongOrNull() ?: 0L
        if (amount <= 0L) {
            viewModelScope.launch { events.emit(ActivePurchaseEvent.Message("Ingresá un importe mayor a cero.")) }
            return
        }
        addItem(amount, quantity)
    }

    fun undoLastItem() {
        viewModelScope.launch {
            runCatching { shoppingRepository.undoLastItem() }
                .onSuccess { item ->
                    if (item == null) {
                        events.emit(ActivePurchaseEvent.Message("No hay productos para deshacer."))
                    } else {
                        events.emit(ActivePurchaseEvent.ItemUndone(item))
                    }
                }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo deshacer el último producto.")) }
        }
    }

    fun restoreItem(item: ShoppingItem) {
        viewModelScope.launch {
            runCatching { shoppingRepository.restoreItem(item) }
                .onSuccess { events.emit(ActivePurchaseEvent.ItemRestored) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo restaurar el producto.")) }
        }
    }

    fun updateBudget(budget: Long?) {
        viewModelScope.launch {
            runCatching { shoppingRepository.updateBudget(budget) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo actualizar el presupuesto.")) }
        }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            runCatching { settingsRepository.setCurrencySymbol(symbol) }
                .onFailure { events.emit(ActivePurchaseEvent.Message("No se pudo guardar la moneda.")) }
        }
    }

    fun setCentsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setCentsEnabled(enabled) }
    }

    fun setVibrateOnAdd(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVibrateOnAdd(enabled) }
    }

    fun setSoundOnAdd(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundOnAdd(enabled) }
    }

    fun setKeepScreenOnDuringPurchase(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenOnDuringPurchase(enabled) }
    }

    fun setHideAmountsOnLockScreen(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHideAmountsOnLockScreen(enabled) }
    }

    fun setConfirmBeforeFinish(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setConfirmBeforeFinish(enabled) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun updateItem(itemId: Long, unitPrice: Long, quantity: Int, name: String?) {
        viewModelScope.launch {
            runCatching { shoppingRepository.updateItem(itemId, unitPrice, quantity, name) }
                .onSuccess { events.emit(ActivePurchaseEvent.Message("Producto actualizado.")) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo actualizar el producto.")) }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            runCatching { shoppingRepository.deleteItem(itemId) }
                .onSuccess { events.emit(ActivePurchaseEvent.Message("Producto eliminado.")) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo eliminar el producto.")) }
        }
    }

    fun finishActiveSession() {
        viewModelScope.launch {
            runCatching { shoppingRepository.finishActiveSession() }
                .onSuccess { sessionId -> events.emit(ActivePurchaseEvent.PurchaseFinished(sessionId)) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo finalizar la compra.")) }
        }
    }

    fun deleteCompletedSession(sessionId: Long) {
        viewModelScope.launch {
            runCatching { shoppingRepository.deleteCompletedSession(sessionId) }
                .onSuccess { events.emit(ActivePurchaseEvent.Message("Compra eliminada.")) }
                .onFailure { events.emit(ActivePurchaseEvent.Message(it.message ?: "No se pudo eliminar la compra.")) }
        }
    }

    fun observeSession(sessionId: Long) = shoppingRepository.observeSession(sessionId)

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

    private companion object {
        const val MAX_INPUT_DIGITS = 10
        const val MAX_PRODUCT_NAME_LENGTH = 48
        const val ADD_DEBOUNCE_MS = 700L
    }
}
