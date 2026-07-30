package com.voygastando.app.ui.screen.activepurchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton
import com.voygastando.app.ui.components.SoftPill
import com.voygastando.app.ui.components.StatRow
import com.voygastando.app.util.MoneyFormatter
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs

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

    LaunchedEffect(events, uiState.appSettings.vibrateOnAdd) {
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
                        message = "Ultimo importe eliminado",
                        actionLabel = "RESTAURAR",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        onRestoreItem(event.item)
                    }
                }
                ActivePurchaseEvent.ItemRestored -> {
                    snackbarHostState.showSnackbar("Importe restaurado", duration = SnackbarDuration.Short)
                }
                is ActivePurchaseEvent.PurchaseFinished -> {
                    snackbarHostState.showSnackbar("Compra finalizada", duration = SnackbarDuration.Short)
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActiveHeader(uiState, formatter, onEditBudget = { showBudgetDialog = true })
            AmountDisplay(uiState, formatter)
            NumericKeypad(
                enabled = !uiState.isAdding,
                onAppendDigit = onAppendDigit,
                onBackspace = onBackspace,
                onClear = onClearInput
            )
            ActionRow(
                enabled = !uiState.isAdding,
                onQuantity = {
                    if (uiState.currentAmount > 0) showQuantityDialog = true else onAddCurrentInput(1)
                },
                onSum = { onAddCurrentInput(1) }
            )
            LastAmountBlock(uiState, formatter, onUndoLastItem)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryActionButton("Productos", onViewItems, modifier = Modifier.weight(1f), height = 44)
                SecondaryActionButton(
                    text = "Finalizar",
                    onClick = {
                        if (uiState.appSettings.confirmBeforeFinish) showFinishDialog = true else onFinishPurchase()
                    },
                    modifier = Modifier.weight(1f),
                    height = 44
                )
            }
        }
    }
}

@Composable
private fun ActiveHeader(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onEditBudget: () -> Unit
) {
    val exceeded = uiState.totals.budgetExceeded != null
    val headerColor = if (exceeded) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.secondary
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("TOTAL DEL CARRITO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatter.format(uiState.totals.total, uiState.moneySettings),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 54.sp,
                    color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            SoftPill(
                text = "${uiState.totals.unitCount} ${if (uiState.totals.unitCount == 1) "producto" else "productos"}",
                color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        BudgetBlock(uiState, formatter, onEditBudget)
    }
}

@Composable
private fun AmountDisplay(uiState: ActivePurchaseUiState, formatter: MoneyFormatter) {
    FigmaCard(
        modifier = Modifier.padding(horizontal = 20.dp),
        padding = 14
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = if (uiState.currentAmount > 0) formatter.format(uiState.currentAmount, uiState.moneySettings) else "$ 0",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = if (uiState.currentAmount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
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
        listOf("00", "0", "<")
    )
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Button(
                        onClick = { if (key == "<") onBackspace() else onAppendDigit(key) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .semantics { contentDescription = if (key == "<") "Borrar ultimo digito" else "Numero $key" },
                        enabled = enabled,
                        shape = RoundedCornerShape(14.dp),
                        border = if (key == "<") null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (key == "<") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            contentColor = if (key == "<") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(key, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        TextButton(onClick = onClear, enabled = enabled, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("BORRAR IMPORTE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionRow(enabled: Boolean, onQuantity: () -> Unit, onSum: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onQuantity,
            modifier = Modifier
                .weight(2f)
                .height(56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("CANT.", fontWeight = FontWeight.Black, fontSize = 15.sp, maxLines = 1)
        }
        PrimaryActionButton("SUMAR", onSum, modifier = Modifier.weight(3f), enabled = enabled, height = 56)
    }
}

@Composable
private fun LastAmountBlock(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onUndoLastItem: () -> Unit
) {
    val lastItem = uiState.lastItem
    if (lastItem == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(44.dp)
        )
        return
    }
    FigmaCard(modifier = Modifier.padding(horizontal = 20.dp), padding = 12) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Ultimo importe", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text(
                    "${formatter.format(lastItem.unitPrice, uiState.moneySettings)} x ${lastItem.quantity}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
            TextButton(onClick = onUndoLastItem) {
                Text("DESHACER", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BudgetBlock(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onEditBudget: () -> Unit
) {
    val budget = uiState.activeSession?.budget
    if (budget == null) {
        TextButton(onClick = onEditBudget) {
            Text("+ Agregar presupuesto", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            "${uiState.totals.recordCount} registros cargados",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        return
    }

    val percent = uiState.totals.budgetUsagePercent ?: 0
    val progress = (percent / 100f).coerceIn(0f, 1f)
    val exceeded = uiState.totals.budgetExceeded
    val available = uiState.totals.budgetRemaining ?: 0
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (exceeded != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            trackColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.10f)
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onEditBudget, modifier = Modifier.height(32.dp)) {
                Text("Presupuesto: ${formatter.format(budget, uiState.moneySettings)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                if (exceeded != null) "Excedido ${formatter.format(exceeded, uiState.moneySettings)}" else "Disponible ${formatter.format(available, uiState.moneySettings)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (exceeded != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
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
        shape = RoundedCornerShape(28.dp),
        title = { Text("Finalizar compra", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Black) },
        text = {
            FigmaCard(padding = 4) {
                Column {
                    StatRow("Total", formatter.format(uiState.totals.total, uiState.moneySettings))
                    StatRow("Productos", uiState.totals.unitCount.toString())
                    StatRow("Registros", uiState.totals.recordCount.toString())
                    if (budget != null) {
                        StatRow("Presupuesto", formatter.format(budget, uiState.moneySettings))
                        StatRow(
                            if ((difference ?: 0) >= 0) "Disponible" else "Excedido",
                            formatter.format(abs(difference ?: 0), uiState.moneySettings),
                            valueColor = if ((difference ?: 0) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(16.dp)) { Text("FINALIZAR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}

@Composable
private fun QuantityDialog(
    unitPrice: Long,
    formatter: MoneyFormatter,
    uiState: ActivePurchaseUiState,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val subtotal = unitPrice * quantity
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Cantidad", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                FigmaCard(padding = 12) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Precio unitario", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(formatter.format(unitPrice, uiState.moneySettings), fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    Button(onClick = { quantity = (quantity - 1).coerceAtLeast(1) }, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(18.dp)) { Text("-", fontSize = 24.sp) }
                    Text(quantity.toString(), fontSize = 48.sp, fontWeight = FontWeight.Black)
                    Button(onClick = { quantity = (quantity + 1).coerceAtMost(99) }, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(18.dp)) { Text("+", fontSize = 24.sp) }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(formatter.format(subtotal, uiState.moneySettings), color = MaterialTheme.colorScheme.primary, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }, shape = RoundedCornerShape(16.dp)) {
                Text("AGREGAR $quantity")
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
        shape = RoundedCornerShape(28.dp),
        title = { Text("Presupuesto", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Dejalo vacio para quitar el presupuesto.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter(Char::isDigit).take(10) },
                    label = { Text("Presupuesto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
                Text("Actual: ${formatter.format(currentBudget ?: 0, uiState.moneySettings)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(budgetText.toLongOrNull()?.takeIf { it > 0 }) }, shape = RoundedCornerShape(16.dp)) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
