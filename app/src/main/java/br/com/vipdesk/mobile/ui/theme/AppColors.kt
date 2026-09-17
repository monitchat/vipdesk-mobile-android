package br.com.vipdesk.mobile.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens do design "VipDesk Mobile v2" (handoff em design_handoff_vipdesk_mobile/).
 * Tema claro; a cor primária é white-label — vem de um único ponto
 * ([WhiteLabel.primary]) e tudo que é "de marca" deriva dela.
 */
@Immutable
data class AppColors(
    // Marca (white-label)
    val primary: Color,
    val primaryDark: Color,     // pressed
    val primaryLight: Color,    // contornos de destaque, ação em toast
    val primarySurface: Color,  // fundo claro derivado

    // Funcionais
    val success: Color,
    val danger: Color,
    val warning: Color,
    val info: Color,

    // Cinzas / superfícies
    val text: Color,            // #111827
    val textSecondary: Color,   // #374151 (design) → aqui usamos p/ rótulos secundários fortes
    val textTertiary: Color,    // #4b5563
    val muted: Color,           // #6b7280
    val placeholder: Color,     // #9ca3af
    val border: Color,          // #d1d5db
    val divider: Color,         // #e5e7eb
    val surfaceAlt: Color,      // #f3f4f6
    val surfaceSoft: Color,     // #f9fafb
    val background: Color,      // #f5f6f8 (fundo de tela)
    val surface: Color,         // #fff (cards, header, sheets)
    val chatBackground: Color,  // #efeae6

    // Balões de chat
    val agentBubble: Color,        // #ede4f2
    val agentBubbleBorder: Color,  // #e0d0e8
    val internalBubble: Color,     // #fef3c7
    val internalBubbleBorder: Color, // #fcd34d
    val internalComposer: Color,   // #fffbeb

    val isDark: Boolean
) {
    // ————— Aliases legados (telas ainda não migradas) —————
    val textPrimary: Color get() = text
    val textFaint: Color get() = placeholder
    val chip: Color get() = surfaceAlt
    val accent: Color get() = primary
    val accentSoft: Color get() = primarySurface
    val accentBorder: Color get() = primaryLight
    val accentStrong: Color get() = primary
    val onAccentStrong: Color get() = Color.White
    val accentTint: Color get() = primary
    val outBubble: Color get() = agentBubble
    val outBubbleBorder: Color get() = agentBubbleBorder
    val noteBg: Color get() = internalBubble
    val noteBorder: Color get() = internalBubbleBorder
    val navBg: Color get() = surface
    val toastBg: Color get() = Color(0xFF111827)
    val toastBorder: Color get() = Color(0xFF111827)
    val toastText: Color get() = Color.White
    val surfaceVariant: Color get() = surfaceAlt
    val fieldBackground: Color get() = surface
    val fieldBorder: Color get() = border
    val iconMuted: Color get() = textTertiary
}

/** Cor primária white-label. Troque aqui (ou via configuração) e tudo deriva. */
object WhiteLabel {
    val primary = Color(0xFF7E3E97)
    val primaryDark = Color(0xFF5B2A70)
    val primaryLight = Color(0xFFA76BC0)
    val primarySurface = Color(0xFFF3ECF7)
}

// Tints semânticos (badges) — fundo/texto
object Tint {
    val blueBg = Color(0xFFDBEAFE); val blueFg = Color(0xFF1D4ED8)
    val yellowBg = Color(0xFFFEF3C7); val yellowFg = Color(0xFF92400E); val yellowDeep = Color(0xFF78350F)
    val amberFg = Color(0xFFB45309)
    val greenBg = Color(0xFFDCFCE7); val greenFg = Color(0xFF166534)
    val mintBg = Color(0xFFECFDF5); val mintFg = Color(0xFF047857)
    val redBg = Color(0xFFFEE2E2); val redFg = Color(0xFFB91C1C)
    val roseBg = Color(0xFFFEF2F2); val roseFg = Color(0xFF991B1B)
    val pinkBg = Color(0xFFFCE7F3); val pinkFg = Color(0xFFBE185D)
    val indigoBg = Color(0xFFE0E7FF); val indigoFg = Color(0xFF3730A3)
    val grayBg = Color(0xFFF3F4F6); val grayFg = Color(0xFF4B5563)
}

// Marcas de canal
object Brand {
    val whatsapp = Color(0xFF16A34A)
    val instagram = Color(0xFFDB2777)
    val messenger = Color(0xFF2563EB)
    val telegram = Color(0xFF0EA5E9)
    val email = Color(0xFF4B5563)
    val webchat = Color(0xFF0891B2)
    val sms = Color(0xFF4B5563)
}

// Compat com código legado que importava estes nomes
val VdSuccess = Color(0xFF22C55E)
val VdWarning = Color(0xFFF59E0B)
val VdDanger = Color(0xFFEF4444)
val VdInfo = Color(0xFF3B82F6)
val VdLilac = Color(0xFFA76BC0)
val VdNeutral = Color(0xFF9CA3AF)

val LightAppColors = AppColors(
    primary = WhiteLabel.primary,
    primaryDark = WhiteLabel.primaryDark,
    primaryLight = WhiteLabel.primaryLight,
    primarySurface = WhiteLabel.primarySurface,
    success = VdSuccess,
    danger = VdDanger,
    warning = VdWarning,
    info = VdInfo,
    text = Color(0xFF111827),
    textSecondary = Color(0xFF6B7280),
    textTertiary = Color(0xFF4B5563),
    muted = Color(0xFF6B7280),
    placeholder = Color(0xFF9CA3AF),
    border = Color(0xFFD1D5DB),
    divider = Color(0xFFE5E7EB),
    surfaceAlt = Color(0xFFF3F4F6),
    surfaceSoft = Color(0xFFF9FAFB),
    background = Color(0xFFF5F6F8),
    surface = Color(0xFFFFFFFF),
    chatBackground = Color(0xFFEFEAE6),
    agentBubble = Color(0xFFEDE4F2),
    agentBubbleBorder = Color(0xFFE0D0E8),
    internalBubble = Color(0xFFFEF3C7),
    internalBubbleBorder = Color(0xFFFCD34D),
    internalComposer = Color(0xFFFFFBEB),
    isDark = false
)

/** Variante escura (cortesia — o design v2 é claro; mantida para o toggle). */
val DarkAppColors = AppColors(
    primary = WhiteLabel.primaryLight,
    primaryDark = WhiteLabel.primary,
    primaryLight = Color(0xFFC49AD6),
    primarySurface = Color(0xFF2E2238),
    success = VdSuccess,
    danger = VdDanger,
    warning = VdWarning,
    info = VdInfo,
    text = Color(0xFFF3F4F6),
    textSecondary = Color(0xFF9CA3AF),
    textTertiary = Color(0xFFB4B8C2),
    muted = Color(0xFF9CA3AF),
    placeholder = Color(0xFF6B7280),
    border = Color(0xFF3A3D47),
    divider = Color(0xFF2A2D36),
    surfaceAlt = Color(0xFF23262E),
    surfaceSoft = Color(0xFF1E2128),
    background = Color(0xFF111318),
    surface = Color(0xFF1A1C22),
    chatBackground = Color(0xFF15171C),
    agentBubble = Color(0xFF2E2238),
    agentBubbleBorder = Color(0xFF3F2F4C),
    internalBubble = Color(0xFF3A2F12),
    internalBubbleBorder = Color(0xFF6B5416),
    internalComposer = Color(0xFF2A2412),
    isDark = true
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
