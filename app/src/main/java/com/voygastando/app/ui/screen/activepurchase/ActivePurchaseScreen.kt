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
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.MoneyDisplay
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton
import com.voygastando.app.ui.components.SoftPill
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
    onViewItems: () -> Unit,
    onFinishPurchase: () -> Unit,
    onPurchaseFinished: (Long) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is ActivePurchaseEvent.ItemAdded -> {
                    if (uiState.appSettings.vibrateOnAdd) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
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
                is ActivePurchaseEvent.PurchaseFinished -> {
                    snackbarHostState.showSnackbar("Compra finalizada.", duration = SnackbarDuration.Short)
                    onPurchaseFinished(event.sessionId)
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

    if (showFinishDialog) {
        FinishPurchaseDialog(
            uiState = uiState,
            formatter = formatter,
            onDismiss = { showFinishDialog = false },
            onConfirm = {
                showFinishDialog = false
                onFinishPurchase()
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        .height(58.dp),
                    enabled = !uiState.isAdding,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("CANTIDAD", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                PrimaryActionButton(
                    text = "SUMAR",
                    onClick = { onAddCurrentInput(1) },
                    modifier = Modifier
                        .weight(1.35f)
                        .height(58.dp),
                    enabled = !uiState.isAdding
                )
            }

            LastAmountBlock(uiState, formatter, onUndoLastItem)

            SecondaryActions(
                onViewItems = onViewItems,
                onFinishPurchase = {
                    if (uiState.appSettings.confirmBeforeFinish) {
                        showFinishDialog = true
                    } else {
                        onFinishPurchase()
                    }
                }
            )
        }
    }
}

@Composable
private fun HeaderTotals(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onEditBudget: () -> Unit
) {
    val exceeded = uiState.totals.budgetExceeded != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (exceeded) androidx.compose.ui.graphics.Color(0xFFFFEEEE) else MaterialTheme.colorScheme.secondary,
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("TOTAL DEL CARRITO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MoneyDisplay(uiState.totals.total, formatter, uiState.moneySettings, large = true)
        BudgetBlock(uiState, formatter, onEditBudget)
        SoftPill(
            text = "${uiState.totals.unitCount} ${if (uiState.totals.unitCount == 1) "producto" else "productos"}",
            color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun CurrentInputDisplay(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter
) {
    FigmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("IMPORTE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (uiState.currentAmount > 0) formatter.format(uiState.currentAmount, uiState.moneySettings) else "$ 0",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = if (uiState.currentAmount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                )
            }
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
                            .height(56.dp)
                            .semantics { contentDescription = if (key == "←") "Borrar último dígito" else "Número $key" },
                        enabled = enabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = if (key == "←") {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.ui.graphics.Color.White,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    ) {
                        Text(key, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        SecondaryActionButton("BORRAR IMPORTE", onClear, enabled = enabled)
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
            fontWeight = FontWeight.Black
        )
        SecondaryActionButton("DESHACER ÚLTIMO", onUndoLastItem)
    }
}

@Composable
private fun SecondaryActions(
    onViewItems: () -> Unit,
    onFinishPurchase: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryActionButton("Ver productos", onViewItems, modifier = Modifier.weight(1f))
            SecondaryActionButton("Finalizar", onFinishPurchase, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FinishPurchaseDialog(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val session = uiState.activeSession
    val budget = session?.budget
    val difference = budget?.let { it - uiState.totals.total }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Finalizar compra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total: ${formatter.format(uiState.totals.total, uiState.moneySettings)}")
                Text("Productos: ${uiState.totals.unitCount}")
                Text("Registros: ${uiState.totals.recordCount}")
                if (budget != null) {
                    Text("Presupuesto: ${formatter.format(budget, uiState.moneySettings)}")
                    val label = if ((difference ?: 0) >= 0) "Disponible" else "Excedido"
                    Text("$label: ${formatter.format(kotlin.math.abs(difference ?: 0), uiState.moneySettings)}")
                }
                val startedAt = session?.startedAt ?: System.currentTimeMillis()
                Text("Duracion: ${formatDuration(System.currentTimeMillis() - startedAt)}")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("FINALIZAR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = (durationMillis / 60_000).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours} h ${minutes} min"
        minutes > 0 -> "${minutes} min"
        else -> "menos de 1 min"
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
            TextButton(onClick = onEditBudget) { Text("+ Agregar presupuesto", fontWeight = FontWeight.Black) }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Presupuesto: ${formatter.format(budget, uiState.moneySettings)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            TextButton(onClick = onEditBudget) { Text("Editar", fontWeight = FontWeight.Black) }
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (uiState.totals.budgetExceeded != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.10f)
        )
        Text("Usado: $percent%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val exceeded = uiState.totals.budgetExceeded
        if (exceeded != null) {
            Text(
                text = "Excedido: ${formatter.format(exceeded, uiState.moneySettings)}",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Black
            )
        } else {
            Text(
                text = "Disponible: ${formatter.format(uiState.totals.budgetRemaining ?: 0, uiState.moneySettings)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        }
    }
}
