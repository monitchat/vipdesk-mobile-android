package br.com.vipdesk.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta semântica que troca entre claro/escuro. Gradientes de marca
 * (roxo/verde) e cores de acento ficam constantes — igual ao mockup,
 * onde só fundo/superfície/texto invertem no dark mode.
 */
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val fieldBackground: Color,
    val fieldBorder: Color,
    val divider: Color,
    val iconMuted: Color,
    val isDark: Boolean
)

val LightAppColors = AppColors(
    background = Color(0xFFF4F4FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF6F4FA),
    textPrimary = Color(0xFF1C1B1F),
    textSecondary = Color(0xFF6B6878),
    fieldBackground = Color(0xFFF6F4FA),
    fieldBorder = Color(0xFFE6E1EE),
    divider = Color(0xFFEDEBF2),
    iconMuted = Color(0xFF9A93A8),
    isDark = false
)

val DarkAppColors = AppColors(
    background = Color(0xFF0F1020),
    surface = Color(0xFF1A1B2E),
    surfaceVariant = Color(0xFF23233A),
    textPrimary = Color(0xFFECECF2),
    textSecondary = Color(0xFFA6A4B5),
    fieldBackground = Color(0xFF23233A),
    fieldBorder = Color(0xFF34344E),
    divider = Color(0xFF2A2A40),
    iconMuted = Color(0xFF7C7A90),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
