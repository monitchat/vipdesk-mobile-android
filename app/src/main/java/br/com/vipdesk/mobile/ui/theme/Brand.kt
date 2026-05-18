package br.com.vipdesk.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens for the VipDesk mobile "soft UI" style:
 * light lavender background, rounded pill inputs, gradient stat cards,
 * circular accents — all built on the VipDesk purple identity (#7E3E97).
 */

// Page / surface neutrals
val AppBackground = Color(0xFFF4F4FB)
val AppSurface = Color(0xFFFFFFFF)
val SoftShadow = Color(0x1A5C2D70)
val FieldBackground = Color(0xFFF6F4FA)
val FieldBorder = Color(0xFFE6E1EE)
val IconMuted = Color(0xFF9A93A8)

// Status dots (conversation presence)
val OnlineGreen = Color(0xFF2FAC66)
val OfflineRed = Color(0xFFE74C3C)
val AwayGray = Color(0xFFBDBDBD)

// Brand gradients (mockup uses a 2-color split: brand + secondary)
val PurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7E3E97), Color(0xFF5C2D70))
)
val PurpleGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFF9B4FB8), Color(0xFF7E3E97))
)
val GreenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF34C77B), Color(0xFF1F8A50))
)
val TealGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF3FB9C9), Color(0xFF2E8FA0))
)

// Login backdrop (very light, like the mockup's near-white screen)
val LoginBackdrop = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1ECF7))
)

// Outgoing chat bubble (own messages) — brand gradient
val OwnBubbleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8E4AA8), Color(0xFF6E3486))
)
