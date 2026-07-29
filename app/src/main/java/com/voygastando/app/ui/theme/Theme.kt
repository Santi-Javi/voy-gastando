package com.voygastando.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = GroceryGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SoftGreen,
    onSecondary = GroceryGreen,
    error = AlertRed,
    onError = androidx.compose.ui.graphics.Color.White,
    background = WarmSurface,
    onBackground = GroceryGreenDark,
    surface = CardWhite,
    onSurface = GroceryGreenDark,
    surfaceVariant = SoftGray,
    onSurfaceVariant = MutedText,
    outline = androidx.compose.ui.graphics.Color(0x170F1C14)
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7AD6A3),
    secondary = androidx.compose.ui.graphics.Color(0xFFF2C46B),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
)

private val AppTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
        displaySmall = base.displaySmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
        headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        titleLarge = base.titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        titleMedium = base.titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        bodyLarge = base.bodyLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium),
        bodyMedium = base.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = base.labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold)
    )
}

@Composable
fun VoyGastandoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
