package com.voygastando.app.ui.screen.activepurchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voygastando.app.ui.components.MoneyDisplay
import com.voygastando.app.util.MoneyFormatter

@Composable
fun ActivePurchaseScreen(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter,
    onAddDemoItem: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
                Spacer(Modifier.height(16.dp))
                BudgetBlock(uiState, formatter)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "${uiState.totals.unitCount} ${if (uiState.totals.unitCount == 1) "producto" else "productos"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column {
                Text(
                    text = "Base de compra activa persistente lista.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddDemoItem,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    Text("SUMAR \$ 1.000")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Teclado numérico en Fase 2")
                }
            }
        }
    }
}

@Composable
private fun BudgetBlock(
    uiState: ActivePurchaseUiState,
    formatter: MoneyFormatter
) {
    val budget = uiState.activeSession?.budget ?: return
    val percent = uiState.totals.budgetUsagePercent ?: 0
    val progress = (percent / 100f).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Presupuesto:", fontWeight = FontWeight.SemiBold)
            Text(formatter.format(budget, uiState.moneySettings))
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth()
        )
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
