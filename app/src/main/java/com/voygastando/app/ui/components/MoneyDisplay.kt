package com.voygastando.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voygastando.app.domain.model.MoneySettings
import com.voygastando.app.util.MoneyFormatter

@Composable
fun MoneyDisplay(
    amount: Long,
    formatter: MoneyFormatter,
    settings: MoneySettings,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Text(
        text = formatter.format(amount, settings),
        modifier = modifier.semantics {
            contentDescription = formatter.forTalkBack(amount, settings)
        },
        style = if (large) {
            MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 54.sp
            )
        } else {
            MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        }
    )
}
