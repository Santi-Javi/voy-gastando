package com.voygastando.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    currentRoute: String?,
    hasActiveSession: Boolean,
    onHome: () -> Unit,
    onActive: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.height(72.dp),
        color = androidx.compose.ui.graphics.Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BottomItem("Inicio", "IN", currentRoute == AppRoute.Home.route, onHome)
            BottomItem("Compra", "+", currentRoute == AppRoute.ActivePurchase.route, onActive, enabled = hasActiveSession)
            BottomItem("Historial", "HI", currentRoute == AppRoute.History.route, onHistory)
            BottomItem("Config.", "CFG", currentRoute == AppRoute.Settings.route, onSettings)
        }
    }
}

@Composable
private fun RowScope.BottomItem(
    label: String,
    icon: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .height(58.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .background(
                if (selected) MaterialTheme.colorScheme.secondary else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = icon,
            color = contentColor,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
