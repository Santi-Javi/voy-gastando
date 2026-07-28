package com.voygastando.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "AR"))
private val timeFormatter = SimpleDateFormat("HH:mm", Locale("es", "AR"))

fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))

fun formatDurationText(startedAt: Long, finishedAt: Long = System.currentTimeMillis()): String {
    val totalMinutes = ((finishedAt - startedAt) / 60_000).coerceAtLeast(0)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours} h ${minutes} min"
        minutes > 0 -> "${minutes} min"
        else -> "menos de 1 min"
    }
}
