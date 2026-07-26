package com.voygastando.app.ui.screen.activepurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.ui.components.MoneyDisplay
import com.voygastando.app.util.MoneyFormatter
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ActivePurchaseScreen(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    events: SharedFlow<ActivePurchaseEvent>,
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearInput: () -> Unit,
    onAddCurrentInput: (Int) -> Unit,
    onUndoLastItem: () -> Unit,
    onRestoreItem: (ShoppingItem) -> Unit,
    onUpdateBudget: (Long?) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ActivePurchaseEvent.ItemAdded -> {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    snackbarHostState.showSnackbar(
                        message = "Importe agregado: ${formatter.format(event.subtotal, uiState.moneySettings)}",
                        duration = SnackbarDuration.Short
                    )
                }
                is ActivePurchaseEvent.ItemUndone -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Último importe eliminado.",
                        actionLabel = "RESTAURAR",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onRestoreItem(event.item)
                    }
                }
                ActivePurchaseEvent.ItemRestored -> {
                    snackbarHostState.showSnackbar("Importe restaurado.", duration = SnackbarDuration.Short)
                }
                is ActivePurchaseEvent.Message -> {
                    snackbarHostState.showSnackbar(event.text, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    if (showQuantityDialog) {
        QuantityDialog(
            unitPrice = uiState.currentAmount,
            formatter = formatter,
            uiState = uiState,
            onDismiss = { showQuantityDialog = false },
            onConfirm = { quantity ->
                showQuantityDialog = false
                onAddCurrentInput(quantity)
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = uiState.activeSession?.budget,
            formatter = formatter,
            uiState = uiState,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { budget ->
                showBudgetDialog = false
                onUpdateBudget(budget)
            }
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeaderTotals(uiState, formatter, onEditBudget = { showBudgetDialog = true })

            CurrentInputDisplay(uiState, formatter)

            NumericKeypad(
                enabled = !uiState.isAdding,
                onAppendDigit = onAppendDigit,
                onBackspace = onBackspace,
                onClear = onClearInput
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (uiState.currentAmount > 0) showQuantityDialog = true else onAddCurrentInput(1)
                    },
                    modifier = Modifier
                        .weight(0.9f)
                        .height(62.dp),
                    enabled = !uiState.isAdding
                ) {
                    Text("CANTIDAD", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onAddCurrentInput(1) },
                    modifier = Modifier
                        .weight(1.35f)
                        .height(62.dp),
                    enabled = !uiState.isAdding
                ) {
                    Text("SUMAR", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            LastAmountBlock(uiState, formatter, onUndoLastItem)

            SecondaryActions()
        }
    }
}

@Composable
private fun HeaderTotals(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onEditBudget: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "TOTAL DEL CARRITO",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        MoneyDisplay(
            amount = uiState.totals.total,
            formatter = formatter,
            settings = uiState.moneySettings,
            large = true
        )
        BudgetBlock(uiState, formatter, onEditBudget)
        Text(
            text = "${uiState.totals.unitCount} ${if (uiState.totals.unitCount == 1) "producto" else "productos"}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CurrentInputDisplay(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("IMPORTE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            MoneyDisplay(
                amount = uiState.currentAmount,
                formatter = formatter,
                settings = uiState.moneySettings,
                large = false
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    enabled: Boolean,
    onAppendDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "←")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            if (key == "←") onBackspace() else onAppendDigit(key)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .semantics { contentDescription = if (key == "←") "Borrar último dígito" else "Número $key" },
                        enabled = enabled,
                        colors = if (key == "←") {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        Text(key, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled
        ) {
            Text("BORRAR IMPORTE")
        }
    }
}

@Composable
private fun LastAmountBlock(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onUndoLastItem: () -> Unit
) {
    val lastItem = uiState.lastItem
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Último importe: ${formatter.format(lastItem?.subtotal ?: 0, uiState.moneySettings)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedButton(
            onClick = onUndoLastItem,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("DESHACER ÚLTIMO", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SecondaryActions() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver productos cargados")
        }
        OutlinedButton(
            onClick = { },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finalizar compra")
        }
    }
}

@Composable
private fun QuantityDialog(
    unitPrice: Long,
    formatter: MoneyFormatter,
    uiState: ActivePurchaseUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(2) }
    val subtotal = unitPrice * quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cantidad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { quantity = (quantity - 1).coerceAtLeast(2) },
                        modifier = Modifier.size(56.dp)
                    ) { Text("-") }
                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { quantity = (quantity + 1).coerceAtMost(99) },
                        modifier = Modifier.size(56.dp)
                    ) { Text("+") }
                }
                Text(
                    text = "Subtotal: ${formatter.format(subtotal, uiState.moneySettings)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }) {
                Text("AGREGAR $quantity PRODUCTOS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

@Composable
private fun BudgetDialog(
    currentBudget: Long?,
    formatter: MoneyFormatter,
    uiState: ActivePurchaseUiState,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    var budgetText by remember(currentBudget) { mutableStateOf(currentBudget?.toString().orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Presupuesto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter(Char::isDigit).take(10) },
                    label = { Text("Presupuesto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Actual: ${formatter.format(currentBudget ?: 0, uiState.moneySettings)}")
                Text("Dejalo vacío para quitar el presupuesto.")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(budgetText.toLongOrNull()?.takeIf { it > 0 }) }) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

@Composable
private fun BudgetBlock(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onEditBudget: () -> Unit
) {
    val budget = uiState.activeSession?.budget
    val percent = uiState.totals.budgetUsagePercent ?: 0
    val progress = (percent / 100f).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (budget == null) {
            OutlinedButton(onClick = onEditBudget, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar presupuesto")
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presupuesto: ${formatter.format(budget, uiState.moneySettings)}", fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onEditBudget) { Text("Editar") }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Usado: $percent%")
        val exceeded = uiState.totals.budgetExceeded
        if (exceeded != null) {
            Text(
                text = "Excedido: ${formatter.format(exceeded, uiState.moneySettings)}",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Disponible: ${formatter.format(uiState.totals.budgetRemaining ?: 0, uiState.moneySettings)}"
            )
        }
    }
}
