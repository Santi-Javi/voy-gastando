package com.voygastando.app.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.ui.components.AppMark
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.PrimaryActionButton
import com.voygastando.app.ui.components.SecondaryActionButton

@Composable
fun HomeScreen(
    onStartShopping: (Long?) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(44.dp))
            AppMark()
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Voy Gastando",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Lleva el control de tu compra",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            FigmaCard(padding = 16) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Presupuesto", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("Podes dejarlo vacio.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { value -> budgetText = value.filter { it.isDigit() }.take(10) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Text(
                                "$",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = { Text("100.000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            PrimaryActionButton(
                text = "INICIAR COMPRA",
                height = 64,
                onClick = { onStartShopping(budgetText.toLongOrNull()?.takeIf { it > 0 }) }
            )
            Spacer(Modifier.height(12.dp))
            SecondaryActionButton("HISTORIAL", onOpenHistory, height = 52)
        }
    }
}
