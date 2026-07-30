package com.voygastando.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.AppSettings
import com.voygastando.app.domain.model.AppThemeMode
import com.voygastando.app.ui.components.FigmaCard
import com.voygastando.app.ui.components.TopBar

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onCurrencySymbolChange: (String) -> Unit,
    onCentsEnabledChange: (Boolean) -> Unit,
    onVibrateChange: (Boolean) -> Unit,
    onSoundChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onHideLockAmountsChange: (Boolean) -> Unit,
    onConfirmBeforeFinishChange: (Boolean) -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var symbolText by remember(settings.moneySettings.currencySymbol) {
        mutableStateOf(settings.moneySettings.currencySymbol)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Configuracion",
                subtitle = "Preferencias locales de la app",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSection("Moneda") {
                Text("Pesos argentinos por defecto", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                OutlinedTextField(
                    value = symbolText,
                    onValueChange = {
                        symbolText = it.take(5)
                        onCurrencySymbolChange(symbolText)
                    },
                    label = { Text("Simbolo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                SettingsToggle("Centavos", "Desactivados por defecto.", settings.moneySettings.centsEnabled, onCentsEnabledChange)
            }

            SettingsSection("Carga rapida") {
                SettingsToggle("Vibracion al agregar", "Confirmacion breve al sumar.", settings.vibrateOnAdd, onVibrateChange)
                SettingsToggle("Sonido al agregar", "Desactivado por defecto.", settings.soundOnAdd, onSoundChange)
                SettingsToggle("Mantener pantalla encendida", "Puede aumentar el consumo de bateria.", settings.keepScreenOnDuringPurchase, onKeepScreenOnChange)
                SettingsToggle("Confirmar antes de finalizar", "Evita cierres accidentales.", settings.confirmBeforeFinish, onConfirmBeforeFinishChange)
            }

            SettingsSection("Privacidad") {
                SettingsToggle("Ocultar importes en pantalla bloqueada", "Se usara en la notificacion persistente.", settings.hideAmountsOnLockScreen, onHideLockAmountsChange)
                Text(
                    "Voy Gastando funciona sin internet, sin cuentas, sin analytics y sin recopilar datos personales.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            SettingsSection("Tema") {
                ThemeSelector(settings.themeMode, onThemeModeChange)
            }

            SettingsSection("Acceso rapido") {
                Text("Mosaico rapido: agrega Voy Gastando desde Ajustes rapidos de Android.", fontSize = 13.sp, lineHeight = 17.sp)
                Text("Boton lateral: en telefonos compatibles, usa la doble pulsacion desde Ajustes del sistema.", fontSize = 13.sp, lineHeight = 17.sp)
            }

            SettingsSection("Informacion") {
                Text("Version 0.1.0", fontWeight = FontWeight.Bold)
                Text("Politica local: la app guarda tus compras solo en este dispositivo.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    FigmaCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ThemeSelector(
    selected: AppThemeMode,
    onSelected: (AppThemeMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeButton("Sistema", AppThemeMode.SYSTEM, selected, onSelected, Modifier.weight(1f))
        ThemeButton("Claro", AppThemeMode.LIGHT, selected, onSelected, Modifier.weight(1f))
        ThemeButton("Oscuro", AppThemeMode.DARK, selected, onSelected, Modifier.weight(1f))
    }
}

@Composable
private fun ThemeButton(
    text: String,
    mode: AppThemeMode,
    selected: AppThemeMode,
    onSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = selected == mode
    OutlinedButton(
        onClick = { onSelected(mode) },
        modifier = modifier.background(
            if (isSelected) MaterialTheme.colorScheme.secondary else androidx.compose.ui.graphics.Color.Transparent,
            RoundedCornerShape(14.dp)
        ),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(
            text,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
