package br.com.vipdesk.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta semântica "Nocturne" (design system do app). Escuro é o tema
 * padrão; o claro inverte fundo/superfície/texto e escurece o acento
 * blurple, mantendo as cores funcionais (sucesso/aviso/erro/canais).
 */
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,   // neutral-500
    val textFaint: Color,       // neutral-600
    val divider: Color,
    val chip: Color,            // fundo sutil p/ ícones, barras vazias, tags neutras
    val accent: Color,
    val accentSoft: Color,      // acento a 12% — fundo de chips selecionados
    val accentBorder: Color,    // accent-700 — contornos de destaque
    val accentStrong: Color,    // accent-800 — fundo do FAB / botão enviar
    val onAccentStrong: Color,  // accent-200 — ícone sobre accentStrong
    val accentTint: Color,      // accent-300 — texto de tags lilás
    val outBubble: Color,       // balão de mensagem enviada
    val outBubbleBorder: Color,
    val noteBg: Color,          // nota interna (âmbar translúcido)
    val noteBorder: Color,
    val navBg: Color,           // barra de navegação inferior
    val toastBg: Color,
    val toastBorder: Color,
    val toastText: Color,
    val isDark: Boolean
) {
    // Aliases para código legado que usava os nomes antigos.
    val surfaceVariant: Color get() = chip
    val fieldBackground: Color get() = surface
    val fieldBorder: Color get() = divider
    val iconMuted: Color get() = textSecondary
}

// Funcionais (iguais nos dois temas — vêm do design)
val VdSuccess = Color(0xFF6FBF9B)
val VdWarning = Color(0xFFD9A86A)
val VdDanger = Color(0xFFD98A8A)
val VdInfo = Color(0xFF8AB0D9)
val VdLilac = Color(0xFFB5ABFC)
val VdNeutral = Color(0xFF9397AB)

val DarkAppColors = AppColors(
    background = Color(0xFF161826),
    surface = Color(0xFF232532),
    textPrimary = Color(0xFFE9E9ED),
    textSecondary = Color(0xFF9397AB),
    textFaint = Color(0xFF75798C),
    divider = Color(0x29E9E9ED),
    chip = Color(0x12E9E9ED),
    accent = Color(0xFF9184D9),
    accentSoft = Color(0x1F9184D9),
    accentBorder = Color(0xFF5D5294),
    accentStrong = Color(0xFF423A6A),
    onAccentStrong = Color(0xFFE7E5FE),
    accentTint = Color(0xFFD2CEFD),
    outBubble = Color(0xFF2B2741),
    outBubbleBorder = Color(0xFF423A6A),
    noteBg = Color(0x22D9A86A),
    noteBorder = Color(0x66D9A86A),
    navBg = Color(0xEB232532),
    toastBg = Color(0xFF292B31),
    toastBorder = Color(0xFF595D6C),
    toastText = Color(0xFFF3F5FE),
    isDark = true
)

val LightAppColors = AppColors(
    background = Color(0xFFEEF0F8),
    surface = Color(0xFFFDFDFF),
    textPrimary = Color(0xFF292B31),
    textSecondary = Color(0xFF75798C),
    textFaint = Color(0xFF9397AB),
    divider = Color(0x1F292B31),
    chip = Color(0x0F292B31),
    accent = Color(0xFF6D5FB5),
    accentSoft = Color(0x1F6D5FB5),
    accentBorder = Color(0xFF8A7DC9),
    accentStrong = Color(0xFFE7E5FE),
    onAccentStrong = Color(0xFF5D5294),
    accentTint = Color(0xFF5D5294),
    outBubble = Color(0xFFE7E5FE),
    outBubbleBorder = Color(0xFFD2CEFD),
    noteBg = Color(0xFFF6ECDB),
    noteBorder = Color(0xFFE4C99A),
    navBg = Color(0xEBFDFDFF),
    toastBg = Color(0xFF292B31),
    toastBorder = Color(0xFF595D6C),
    toastText = Color(0xFFF3F5FE),
    isDark = false
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
