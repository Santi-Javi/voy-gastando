package com.voygastando.app.ui.screen.summary

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.ui.components.CheckMark
import com.voygastando.app.ui.components.EmptyState
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton
import com.voygastando.app.ui.components.SoftPill
import com.voygastando.app.ui.components.StatRow
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

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (session == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState("No se encontro la compra", "Volve al inicio para continuar.")
                SecondaryActionButton("Volver al inicio", onBackHome)
            }
            return@Scaffold
        }

        val units = session.items.sumOf { it.quantity }
        val records = session.items.size
        val average = if (units == 0) 0 else session.total / units
        val budget = session.budget
        val difference = budget?.let { it - session.total }
        val exceeded = difference != null && difference < 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 30.dp, bottom = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CheckMark()
                Text("Compra finalizada", color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(formatDate(session.startedAt), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f), fontSize = 14.sp)
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-18).dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FigmaCard(padding = 18) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TOTAL GASTADO", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        Text(formatter.format(session.total, moneySettings), fontSize = 46.sp, fontWeight = FontWeight.Black, lineHeight = 50.sp)
                        if (budget != null) {
                            SoftPill(
                                text = if (exceeded) {
                                    "Excedido: ${formatter.format(abs(difference ?: 0), moneySettings)}"
                                } else {
                                    "Disponible: ${formatter.format(abs(difference ?: 0), moneySettings)}"
                                },
                                color = if (exceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        StatRow("Fecha", formatDate(session.startedAt))
                        StatRow("Horario", "${formatTime(session.startedAt)} - ${formatTime(session.finishedAt ?: session.startedAt)}")
                        StatRow("Duracion", formatDurationText(session.startedAt, session.finishedAt ?: session.startedAt))
                        if (budget != null) {
                            StatRow("Presupuesto", formatter.format(budget, moneySettings))
                        }
                        StatRow("Unidades", units.toString())
                        StatRow("Registros", records.toString())
                        StatRow("Promedio", formatter.format(average, moneySettings))
                    }
                }
                PrimaryActionButton("NUEVA COMPRA", onNewPurchase, height = 56)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryActionButton("VER DETALLE", { onViewDetail(session.id) }, modifier = Modifier.weight(1f))
                    SecondaryActionButton(
                        text = "COMPARTIR",
                        onClick = {
                            val shareText = buildShareText(session, moneySettings, formatter)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                SecondaryActionButton("VOLVER AL INICIO", onBackHome, height = 44)
                Box(Modifier.padding(bottom = 8.dp))
            }
        }
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
        appendLine("Voy Gastando")
        appendLine("Compra finalizada")
        appendLine("Total: ${formatter.format(session.total, moneySettings)}")
        appendLine("Productos: $units")
        if (budget != null) {
            appendLine("Presupuesto: ${formatter.format(budget, moneySettings)}")
            appendLine("${if ((difference ?: 0) >= 0) "Disponible" else "Excedido"}: ${formatter.format(abs(difference ?: 0), moneySettings)}")
        }
    }
}
