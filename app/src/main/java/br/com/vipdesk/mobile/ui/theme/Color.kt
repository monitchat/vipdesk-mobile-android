package br.com.vipdesk.mobile.ui.theme

import androidx.compose.ui.graphics.Color

// Cores estáticas legadas, alinhadas aos tokens v2. Código novo deve usar
// AppTheme.colors / Tint / Brand.
val VipDeskPurple = WhiteLabel.primary
val VipDeskPurpleDark = WhiteLabel.primaryDark
val VipDeskPurpleLight = WhiteLabel.primaryLight
val VipDeskPurpleContainer = WhiteLabel.primarySurface

val VipDeskGreen = VdSuccess
val VipDeskRed = VdDanger
val VipDeskOrange = VdWarning
val VipDeskBlue = VdInfo

// Chat
val SentMessageBg = Color(0xFFEDE4F2)
val ReceivedMessageBg = Color(0xFFFFFFFF)
val CommentBg = Color(0xFFFEF3C7)

// Canais
val WhatsAppGreen = Brand.whatsapp
val EmailBlue = Color(0xFF2563EB)
val WebChatGray = Brand.webchat
val PhoneOrange = VdWarning
val InstagramPink = Brand.instagram
val MessengerBlue = Brand.messenger
val SmsGray = Brand.sms

// Neutros
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF111318)
val CardBorder = Color(0xFFE5E7EB)
val TextSecondary = Color(0xFF6B7280)
val DividerColor = Color(0xFFE5E7EB)

// Avatares: contatos usam cinza neutro (#e5e7eb / #374151) no design v2;
// agentes usam primary-surface / primary.
val AvatarGreen = Color(0xFFE5E7EB)
val AvatarColors = listOf(Color(0xFFE5E7EB))

fun avatarColorFor(name: String): Color = Color(0xFFE5E7EB)
