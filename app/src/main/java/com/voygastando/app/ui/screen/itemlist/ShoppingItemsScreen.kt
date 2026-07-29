package com.voygastando.app.ui.screen.itemlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingItem
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.ui.components.EmptyState
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.SecondaryActionButton
import com.voygastando.app.ui.components.TopBar
import com.voygastando.app.util.MoneyFormatter
import com.voygastando.app.util.formatTime

@Composable
fun ShoppingItemsScreen(
    session: ShoppingSession?,
    moneySettings: MoneySettings,
    formatter: MoneyFormatter,
    readOnly: Boolean,
    onBack: () -> Unit,
    onEditItem: (Long, Long, Int) -> Unit,
    onDeleteItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var newestFirst by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) }
    var deletingItem by remember { mutableStateOf<ShoppingItem?>(null) }

    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            formatter = formatter,
            moneySettings = moneySettings,
            onDismiss = { editingItem = null },
            onConfirm = { price, quantity ->
                editingItem = null
                onEditItem(item.id, price, quantity)
            }
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Eliminar producto") },
            text = { Text("Esta accion recalcula el total de la compra.") },
            confirmButton = {
                Button(onClick = {
                    deletingItem = null
                    onDeleteItem(item.id)
                }) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) { Text("CANCELAR") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Productos cargados",
                subtitle = "Total: ${formatter.format(session?.total ?: 0, moneySettings)}",
                onBack = onBack,
                action = {
                    OutlinedButton(onClick = { newestFirst = !newestFirst }) {
                        Text(if (newestFirst) "Recientes" else "Antiguos")
                    }
                }
            )
        }
    ) { padding ->
        val items = session?.items.orEmpty().let { list ->
            if (newestFirst) list.sortedByDescending { it.sortOrder } else list.sortedBy { it.sortOrder }
        }

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState("Todavía no hay productos", "Los importes que sumes van a aparecer acá.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items) { index, item ->
                ShoppingItemRow(
                    index = index + 1,
                    item = item,
                    formatter = formatter,
                    moneySettings = moneySettings,
                    readOnly = readOnly,
                    onEdit = { editingItem = item },
                    onDelete = { deletingItem = item }
                )
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    index: Int,
    item: ShoppingItem,
    formatter: MoneyFormatter,
    moneySettings: MoneySettings,
    readOnly: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    FigmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$index.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${formatter.format(item.unitPrice, moneySettings)} x ${item.quantity} = ${formatter.format(item.subtotal, moneySettings)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(formatTime(item.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            if (!readOnly) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("Editar", onEdit, modifier = Modifier.weight(1f))
                    SecondaryActionButton("Eliminar", onDelete, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    item: ShoppingItem,
    formatter: MoneyFormatter,
    moneySettings: MoneySettings,
    onDismiss: () -> Unit,
    onConfirm: (Long, Int) -> Unit
) {
    var priceText by remember(item.id) { mutableStateOf(item.unitPrice.toString()) }
    var quantityText by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    val price = priceText.toLongOrNull() ?: 0L
    val quantity = quantityText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit).take(10) },
                    label = { Text("Precio unitario") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit).take(2) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Text("Subtotal: ${formatter.format(price * quantity.coerceAtLeast(0), moneySettings)}")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(price, quantity) },
                enabled = price > 0 && quantity in 1..99,
                modifier = Modifier.height(48.dp)
            ) { Text("GUARDAR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
