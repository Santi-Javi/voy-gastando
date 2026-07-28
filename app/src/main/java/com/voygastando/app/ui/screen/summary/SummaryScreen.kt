package com.voygastando.app.ui.screen.summary

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.util.MoneyFormatter
import com.voygastando.app.util.formatDate
import com.voygastando.app.util.formatDurationText
import com.voygastando.app.util.formatTime
import kotlin.math.abs

@Composable
fun SummaryScreen(
    session: ShoppingSession?,
    moneySettings: MoneySettings,
    formatter: MoneyFormatter,
    onNewPurchase: () -> Unit,
    onViewDetail: (Long) -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(modifier = modifier) { padding ->
        if (session == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("No se encontro la compra.")
                OutlinedButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) { Text("Volver al inicio") }
            }
            return@Scaffold
        }

        val units = session.items.sumOf { it.quantity }
        val records = session.items.size
        val average = if (units == 0) 0 else session.total / units
        val budget = session.budget
        val difference = budget?.let { it - session.total }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Compra finalizada", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            SummaryLine("Fecha", formatDate(session.startedAt))
            SummaryLine("Horario", "${formatTime(session.startedAt)} - ${formatTime(session.finishedAt ?: session.startedAt)}")
            SummaryLine("Duracion", formatDurationText(session.startedAt, session.finishedAt ?: session.startedAt))
            SummaryLine("Total gastado", formatter.format(session.total, moneySettings))
            if (budget != null) {
                SummaryLine("Presupuesto", formatter.format(budget, moneySettings))
                SummaryLine(
                    if ((difference ?: 0) >= 0) "Disponible" else "Excedido",
                    formatter.format(abs(difference ?: 0), moneySettings)
                )
            }
            SummaryLine("Productos", units.toString())
            SummaryLine("Registros", records.toString())
            SummaryLine("Promedio", formatter.format(average, moneySettings))

            Button(onClick = onNewPurchase, modifier = Modifier.fillMaxWidth()) { Text("NUEVA COMPRA") }
            OutlinedButton(onClick = { onViewDetail(session.id) }, modifier = Modifier.fillMaxWidth()) { Text("VER DETALLE") }
            OutlinedButton(
                onClick = {
                    val shareText = buildShareText(session, moneySettings, formatter)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("COMPARTIR RESUMEN") }
            OutlinedButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) { Text("VOLVER AL INICIO") }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value)
    }
}

private fun buildShareText(
    session: ShoppingSession,
    moneySettings: MoneySettings,
    formatter: MoneyFormatter
): String {
    val units = session.items.sumOf { it.quantity }
    val budget = session.budget
    val difference = budget?.let { it - session.total }
    return buildString {
        appendLine("Compra finalizada")
        appendLine("Total: ${formatter.format(session.total, moneySettings)}")
        appendLine("Productos: $units")
        if (budget != null) {
            appendLine("Presupuesto: ${formatter.format(budget, moneySettings)}")
            appendLine("${if ((difference ?: 0) >= 0) "Disponible" else "Excedido"}: ${formatter.format(abs(difference ?: 0), moneySettings)}")
        }
    }
}
