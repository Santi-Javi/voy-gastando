package com.voygastando.app.ui.screen.itemlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    onEditItem: (Long, Long, Int, String?) -> Unit,
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
            onConfirm = { price, quantity, name ->
                editingItem = null
                onEditItem(item.id, price, quantity, name)
            }
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Eliminar producto", fontWeight = FontWeight.Black) },
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
                title = if (readOnly) "Detalle de compra" else "Productos cargados",
                subtitle = "Total: ${formatter.format(session?.total ?: 0, moneySettings)}",
                onBack = onBack,
                action = {
                    TextButton(onClick = { newestFirst = !newestFirst }) {
                        Text(if (newestFirst) "Recientes" else "Antiguos", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Text(
                        formatter.format(session?.total ?: 0, moneySettings),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
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
                EmptyState("Todavia no hay productos", "Los importes que sumes van a aparecer aca.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
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
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("$index.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Column(Modifier.weight(1f)) {
            if (!item.name.isNullOrBlank()) {
                Text(
                    text = item.name,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "${formatter.format(item.unitPrice, moneySettings)} x ${item.quantity} = ${formatter.format(item.subtotal, moneySettings)}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(formatTime(item.createdAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        if (!readOnly) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit, modifier = Modifier.height(32.dp)) {
                    Text("Editar", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
                TextButton(onClick = onDelete, modifier = Modifier.height(32.dp)) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Black, fontSize = 12.sp)
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
    onConfirm: (Long, Int, String?) -> Unit
) {
    var nameText by remember(item.id) { mutableStateOf(item.name.orEmpty()) }
    var priceText by remember(item.id) { mutableStateOf(item.unitPrice.toString()) }
    var quantityText by remember(item.id) { mutableStateOf(item.quantity.toString()) }
    val price = priceText.toLongOrNull() ?: 0L
    val quantity = quantityText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Editar producto", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it.take(48) },
                    label = { Text("Producto opcional") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit).take(10) },
                    label = { Text("Precio unitario") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit).take(2) },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
                FigmaCard(padding = 12) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(formatter.format(price * quantity.coerceAtLeast(0), moneySettings), color = MaterialTheme.colorScheme.primary, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(price, quantity, nameText.trim().takeIf { it.isNotBlank() }) },
                enabled = price > 0 && quantity in 1..99,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text("GUARDAR") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
