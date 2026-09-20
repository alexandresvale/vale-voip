package com.valevoip.core.designsystem.extension

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Converte um timestamp (Long) para uma String de data (dd/MM/yyyy).
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Converte um timestamp (Long) para uma String apenas com hora e minuto (HH:mm).
 */
fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Converte um timestamp (Long) para uma String de data completa com hora.
 * Exemplo: 28/02/2024 às 14:30
 */
fun Long.toFormattedDateTime(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(Date(this))
}
