package com.voygastando.app.ui.screen.summary

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.domain.model.ShoppingSession
import com.voygastando.app.ui.components.EmptyState
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton
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
                EmptyState("No se encontró la compra", "Volvé al inicio para continuar.")
                SecondaryActionButton("Volver al inicio", onBackHome)
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 28.dp, bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                Text("Compra finalizada", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineSmall)
                Text(formatDate(session.startedAt), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f), fontSize = 14.sp)
            }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FigmaCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("TOTAL GASTADO", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        Text(formatter.format(session.total, moneySettings), fontSize = 46.sp, fontWeight = FontWeight.Black)
                        if (budget != null) {
                            val label = if ((difference ?: 0) >= 0) "Disponible" else "Excedido"
                            val color = if ((difference ?: 0) >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            Text(
                                "$label: ${formatter.format(abs(difference ?: 0), moneySettings)}",
                                color = color,
                                fontWeight = FontWeight.Black
                            )
                        }
                        SummaryLine("Horario", "${formatTime(session.startedAt)} - ${formatTime(session.finishedAt ?: session.startedAt)}")
                        SummaryLine("Duración", formatDurationText(session.startedAt, session.finishedAt ?: session.startedAt))
                        SummaryLine("Productos", units.toString())
                        SummaryLine("Registros", records.toString())
                        SummaryLine("Promedio", formatter.format(average, moneySettings))
                    }
                }
                PrimaryActionButton("NUEVA COMPRA", onNewPurchase)
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
                SecondaryActionButton("VOLVER AL INICIO", onBackHome)
            }
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
