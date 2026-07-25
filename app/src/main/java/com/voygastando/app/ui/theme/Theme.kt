package com.voygastando.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GroceryGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = BudgetAmber,
    error = AlertRed,
    surface = WarmSurface
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7AD6A3),
    secondary = androidx.compose.ui.graphics.Color(0xFFF2C46B),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
)

@Composable
fun VoyGastandoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
