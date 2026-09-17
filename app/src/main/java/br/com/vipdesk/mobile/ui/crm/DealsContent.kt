package br.com.vipdesk.mobile.ui.crm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import br.com.vipdesk.mobile.ui.theme.Tint
import java.text.NumberFormat
import java.util.Locale

/** Sinaliza às telas de negócios que algo mudou (criação/movimentação/fechamento). */
object CrmEvents {
    var dealsVersion by mutableStateOf(0)
    var contactsVersion by mutableStateOf(0)
}

fun formatDealValue(value: Double?, currency: String?): String {
    if (value == null || value == 0.0) return "—"
    return try {
        NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
    } catch (_: Exception) {
        "${currency ?: "R$"} $value"
    }
}

/** Rótulo + (fundo, texto) do status do negócio. */
fun dealStatusVisual(status: String?): Triple<String, Color, Color> = when (status) {
    "won" -> Triple("Ganho", Tint.greenBg, Tint.greenFg)
    "lost" -> Triple("Perdido", Tint.redBg, Tint.redFg)
    else -> Triple("Aberto", Tint.blueBg, Tint.blueFg)
}
