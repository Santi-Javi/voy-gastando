package com.voygastando.app.ui.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.ui.components.EmptyState
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton
import com.voygastando.app.ui.components.TopBar
import com.voygastando.app.util.MoneyFormatter
import com.voygastando.app.util.formatDate
import com.voygastando.app.util.formatDurationText

@Composable
fun HistoryScreen(
    sessions: List<ShoppingSession>,
    moneySettings: MoneySettings,
    formatter: MoneyFormatter,
    onBack: () -> Unit,
    onOpenSummary: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var deletingSession by remember { mutableStateOf<ShoppingSession?>(null) }

    deletingSession?.let { session ->
        AlertDialog(
            onDismissRequest = { deletingSession = null },
            title = { Text("Eliminar compra") },
            text = { Text("Se eliminara esta compra finalizada y sus productos.") },
            confirmButton = {
                Button(onClick = {
                    deletingSession = null
                    onDelete(session.id)
                }) { Text("ELIMINAR") }
            },
            dismissButton = {
                TextButton(onClick = { deletingSession = null }) { Text("CANCELAR") }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(title = "Historial", subtitle = "${sessions.size} compras finalizadas", onBack = onBack)
        }
    ) { padding ->
        if (sessions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState("Todavía no hay compras", "Cuando finalices una compra, va a aparecer en este historial.")
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
            items(sessions, key = { it.id }) { session ->
                HistoryRow(
                    session = session,
                    moneySettings = moneySettings,
                    formatter = formatter,
                    onOpen = { onOpenSummary(session.id) },
                    onDelete = { deletingSession = session }
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    session: ShoppingSession,
    moneySettings: MoneySettings,
    formatter: MoneyFormatter,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val units = session.items.sumOf { it.quantity }
    val budget = session.budget
    val difference = budget?.let { it - session.total }
    FigmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(formatDate(session.startedAt), fontWeight = FontWeight.Black)
                    Text("${formatDurationText(session.startedAt, session.finishedAt ?: session.startedAt)} · $units productos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                Text(formatter.format(session.total, moneySettings), fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            if (budget != null) {
                val label = if ((difference ?: 0) >= 0) "Disponible" else "Excedido"
                val color = if ((difference ?: 0) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Text(
                    "$label: ${formatter.format(kotlin.math.abs(difference ?: 0), moneySettings)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryActionButton("Abrir", onOpen, modifier = Modifier.weight(1f))
                SecondaryActionButton("Eliminar", onDelete, modifier = Modifier.weight(1f))
            }
        }
    }
}
