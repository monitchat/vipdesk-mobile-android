package br.com.vipdesk.mobile.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.vipdesk.mobile.ui.theme.EmailBlue
import br.com.vipdesk.mobile.ui.theme.PhoneOrange
import br.com.vipdesk.mobile.ui.theme.TextSecondary
import br.com.vipdesk.mobile.ui.theme.WebChatGray
import br.com.vipdesk.mobile.ui.theme.WhatsAppGreen

fun initialsOf(fullName: String): String {
    return fullName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "?" }
}

private fun parseFlexibleDate(dateStr: String): java.util.Date? {
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
    )
    for (p in patterns) {
        try {
            val sdf = java.text.SimpleDateFormat(p, java.util.Locale("pt", "BR"))
            return sdf.parse(dateStr) ?: continue
        } catch (_: Exception) {
        }
    }
    return null
}

fun relativeTime(dateStr: String): String {
    return try {
        val date = parseFlexibleDate(dateStr) ?: return dateStr
        val diff = System.currentTimeMillis() - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24
        when {
            minutes < 1 -> "agora"
            minutes < 60 -> "${minutes}min"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> java.text.SimpleDateFormat("dd/MM", java.util.Locale("pt", "BR")).format(date)
        }
    } catch (_: Exception) {
        dateStr
    }
}

data class SourceVisual(val icon: ImageVector, val color: Color)

fun sourceVisual(source: String?): SourceVisual = when (source) {
    "whatsapp" -> SourceVisual(Icons.Default.Forum, WhatsAppGreen)
    "facebook" -> SourceVisual(Icons.Default.Facebook, Color(0xFF1877F2))
    "instagram" -> SourceVisual(Icons.Default.CameraAlt, Color(0xFFE4405F))
    "telegram" -> SourceVisual(Icons.AutoMirrored.Filled.Send, Color(0xFF0088CC))
    "email" -> SourceVisual(Icons.Default.Email, EmailBlue)
    "monitcall", "phone" -> SourceVisual(Icons.Default.Phone, PhoneOrange)
    "webchat" -> SourceVisual(Icons.Default.Language, WebChatGray)
    "campaing" -> SourceVisual(Icons.Default.Campaign, TextSecondary)
    else -> SourceVisual(Icons.Default.Forum, TextSecondary)
}
