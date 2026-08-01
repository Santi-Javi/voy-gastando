package com.voygastando.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val items = buildList {
        add(BottomNavItem("Inicio", currentRoute == AppRoute.Home.route, onHome))
        if (hasActiveSession) {
            add(BottomNavItem("Compra", currentRoute == AppRoute.ActivePurchase.route, onActive))
        }
        add(BottomNavItem("Historial", currentRoute == AppRoute.History.route, onHistory))
        add(BottomNavItem("Config.", currentRoute == AppRoute.Settings.route, onSettings))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { item ->
                    BottomItem(item)
                }
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun RowScope.BottomItem(item: BottomNavItem) {
    val contentColor = if (item.selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .background(
                if (item.selected) MaterialTheme.colorScheme.secondary else androidx.compose.ui.graphics.Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = item.onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.label,
            color = contentColor,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
