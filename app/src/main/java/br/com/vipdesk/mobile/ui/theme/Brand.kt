package br.com.vipdesk.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Tokens legados de marca (compatibilidade). Código novo: AppTheme.colors.
val AppBackground = Color(0xFFF5F6F8)
val AppSurface = Color(0xFFFFFFFF)
val SoftShadow = Color(0x24000000)
val FieldBackground = Color(0xFFFFFFFF)
val FieldBorder = Color(0xFFD1D5DB)
val IconMuted = Color(0xFF4B5563)

val OnlineGreen = VdSuccess
val OfflineRed = VdDanger
val AwayGray = Color(0xFF9CA3AF)

val PurpleGradient = Brush.linearGradient(listOf(WhiteLabel.primary, WhiteLabel.primaryDark))
val PurpleGradientLight = Brush.linearGradient(listOf(WhiteLabel.primaryLight, WhiteLabel.primary))
val GreenGradient = Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF166534)))
val TealGradient = Brush.linearGradient(listOf(Color(0xFF0891B2), Color(0xFF155E75)))
val LoginBackdrop = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF5F6F8)))
val OwnBubbleGradient = Brush.linearGradient(listOf(Color(0xFFEDE4F2), Color(0xFFEDE4F2)))
