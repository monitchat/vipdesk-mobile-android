package br.com.vipdesk.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// Cores estáticas legadas, reafinadas para a paleta Nocturne. Código novo
// deve preferir AppTheme.colors / Tokens.kt; estas ficam para componentes
// que ainda não migraram (mídia do chat etc.).
val VipDeskPurple = Color(0xFF9184D9)
val VipDeskPurpleDark = Color(0xFF5D5294)
val VipDeskPurpleLight = Color(0xFFB5ABFC)
val VipDeskPurpleContainer = Color(0xFFE7E5FE)

// Funcionais
val VipDeskGreen = Color(0xFF6FBF9B)
val VipDeskRed = Color(0xFFD98A8A)
val VipDeskOrange = Color(0xFFD9A86A)
val VipDeskBlue = Color(0xFF8AB0D9)

// Chat
val SentMessageBg = Color(0xFF2B2741)
val ReceivedMessageBg = Color(0xFF232532)
val CommentBg = Color(0x22D9A86A)

// Canais (paleta do design)
val WhatsAppGreen = Color(0xFF57B284)
val EmailBlue = Color(0xFF7A94C9)
val WebChatGray = Color(0xFF7FB8B0)
val PhoneOrange = Color(0xFFD9A86A)
val InstagramPink = Color(0xFFC47AB0)
val MessengerBlue = Color(0xFF7A94C9)
val SmsGray = Color(0xFF9C9FB3)

// Neutros
val SurfaceLight = Color(0xFFFDFDFF)
val SurfaceDark = Color(0xFF161826)
val CardBorder = Color(0x1F292B31)
val TextSecondary = Color(0xFF9397AB)
val DividerColor = Color(0x29E9E9ED)

// Avatares (paleta AV do design)
val AvatarGreen = Color(0xFF3F6B5A)
val AvatarColors = listOf(
    Color(0xFF5D5294), Color(0xFF3F6B5A), Color(0xFF6B4A5E),
    Color(0xFF3F5A6B), Color(0xFF6B5A3F), Color(0xFF4A4F6B), Color(0xFF565B3F)
)

fun avatarColorFor(name: String): Color =
    if (name.isBlank()) AvatarColors[0]
    else AvatarColors[Math.floorMod(name.hashCode(), AvatarColors.size)]
