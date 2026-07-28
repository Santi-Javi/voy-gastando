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
import androidx.compose.material3.HorizontalDivider
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
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Historial", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
            }
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
                Text("Todavia no hay compras finalizadas.")
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(formatDate(session.startedAt), fontWeight = FontWeight.Bold)
        Text("Total: ${formatter.format(session.total, moneySettings)}")
        Text("Productos: $units")
        if (budget != null) {
            val label = if ((difference ?: 0) >= 0) "Disponible" else "Excedido"
            Text("$label: ${formatter.format(kotlin.math.abs(difference ?: 0), moneySettings)}")
        }
        Text("Duracion: ${formatDurationText(session.startedAt, session.finishedAt ?: session.startedAt)}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onOpen, modifier = Modifier.weight(1f)) { Text("Abrir") }
            OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Eliminar") }
        }
        HorizontalDivider()
    }
}
