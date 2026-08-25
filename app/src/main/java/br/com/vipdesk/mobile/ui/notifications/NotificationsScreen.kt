package br.com.vipdesk.mobile.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.MobileNotification
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdInfo
import br.com.vipdesk.mobile.ui.theme.VdLilac
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.VdWarning
import br.com.vipdesk.mobile.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun kindVisual(kind: String): Pair<ImageVector, Color> = when (kind.lowercase()) {
    "message", "conversation" -> Icons.Outlined.Forum to WhatsAppGreen
    "sla" -> Icons.Outlined.Timer to VdDanger
    "ticket" -> Icons.Outlined.ConfirmationNumber to VdInfo
    "lead", "contact" -> Icons.Outlined.PersonAddAlt to VdLilac
    "mention" -> Icons.Outlined.AlternateEmail to Color(0xFFC47AB0)
    "board", "task" -> Icons.Outlined.TableChart to VdWarning
    "resolved" -> Icons.Outlined.CheckCircle to VdSuccess
    else -> Icons.Outlined.Notifications to VdInfo
}

private fun groupLabel(createdAt: String?): String {
    if (createdAt == null) return "Anteriores"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pt", "BR"))
        val date = sdf.parse(createdAt) ?: return "Anteriores"
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        val minutes = (now.timeInMillis - then.timeInMillis) / 60000
        when {
            minutes < 15 -> "Agora"
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR) -> "Hoje"
            now.timeInMillis - then.timeInMillis < 48 * 3600_000L -> "Ontem"
            else -> "Anteriores"
        }
    } catch (_: Exception) {
        "Anteriores"
    }
}

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
            }
            Text(
                "Notificações",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (state.unreadCount > 0) {
                Text(
                    "Marcar tudo como lido",
                    fontSize = 12.5.sp,
                    color = c.accent,
                    modifier = Modifier
                        .clickable { viewModel.markAllRead() }
                        .padding(4.dp)
                )
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }
            }
            state.items.isEmpty() -> {
                VdEmptyState(
                    icon = Icons.Outlined.CheckCircle,
                    title = "Tudo em dia",
                    subtitle = "Nenhuma notificação pendente.",
                    iconTint = VdSuccess,
                    iconBg = VdSuccess.copy(alpha = 0.12f)
                )
            }
            else -> {
                val groups = state.items.groupBy { groupLabel(it.createdAt) }
                val order = listOf("Agora", "Hoje", "Ontem", "Anteriores")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 40.dp
                    )
                ) {
                    order.forEach { group ->
                        val items = groups[group] ?: return@forEach
                        item(key = "header-$group") {
                            VdSectionLabel(group, Modifier.padding(top = 14.dp, bottom = 4.dp))
                        }
                        items.forEach { notif ->
                            item(key = notif.id) {
                                NotificationRow(notif) { viewModel.markRead(notif.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notif: MobileNotification, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (icon, tint) = kindVisual(notif.kind)
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(c.chip, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    notif.message,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = c.textPrimary
                )
                notif.createdAt?.let {
                    Text(
                        relativeTime(it),
                        fontSize = 11.sp,
                        color = c.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (!notif.read) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .background(c.accent, CircleShape)
                )
            }
        }
        VdDivider()
    }
}
