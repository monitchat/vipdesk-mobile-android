package br.com.vipdesk.mobile.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Tokens legados de marca, reafinados para a paleta Nocturne. Código novo
 * deve usar AppTheme.colors; estes seguem apenas para compatibilidade.
 */

// Neutros de página/superfície (tema escuro padrão)
val AppBackground = Color(0xFF161826)
val AppSurface = Color(0xFF232532)
val SoftShadow = Color(0x40000000)
val FieldBackground = Color(0xFF232532)
val FieldBorder = Color(0x29E9E9ED)
val IconMuted = Color(0xFF9397AB)

// Pontos de presença
val OnlineGreen = Color(0xFF6FBF9B)
val OfflineRed = Color(0xFFD98A8A)
val AwayGray = Color(0xFF9397AB)

// Gradientes de marca (blurple Nocturne)
val PurpleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF5D5294), Color(0xFF423A6A))
)
val PurpleGradientLight = Brush.linearGradient(
    colors = listOf(Color(0xFF9184D9), Color(0xFF5D5294))
)
val GreenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF6FBF9B), Color(0xFF3F6B5A))
)
val TealGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF7FB8B0), Color(0xFF3F5A6B))
)

// Fundo do login (radial escuro do design fica no próprio LoginScreen)
val LoginBackdrop = Brush.verticalGradient(
    colors = listOf(Color(0xFF1D2036), Color(0xFF161826))
)

// Balão de mensagem enviada
val OwnBubbleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2B2741), Color(0xFF2B2741))
)
