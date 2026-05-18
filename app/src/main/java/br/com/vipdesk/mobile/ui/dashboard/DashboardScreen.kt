package br.com.vipdesk.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.RecentTicket
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.SectionCard
import br.com.vipdesk.mobile.ui.components.StatusBadge
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.theme.*

@Composable
fun DashboardScreen(
    onTicketClick: (Int) -> Unit,
    onSeeAllTickets: () -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val d = uiState.dashboard
    val c = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        // Greeting header + bell + avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Olá, ${uiState.userName}! 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aqui está o resumo do seu dia",
                    fontSize = 13.sp,
                    color = c.textSecondary
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(c.surface)
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = VipDeskPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (uiState.unreadNotifications > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(VipDeskRed)
                            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.unreadNotifications > 9) "9+" else uiState.unreadNotifications.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(PurpleGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(uiState.userName),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stat cards 2x2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Tickets abertos",
                value = d.ticketsOpen.value.toString(),
                icon = Icons.Default.ConfirmationNumber,
                accent = VipDeskPurple,
                trend = d.ticketsOpen.deltaLabel,
                trendPositive = d.ticketsOpen.deltaPct >= 0
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "SLA",
                value = "${d.sla.value}${d.sla.unit}",
                icon = Icons.Default.CheckCircle,
                accent = if (d.sla.within) VipDeskGreen else VipDeskRed,
                trend = d.sla.status,
                trendPositive = d.sla.within
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Tempo médio de resposta",
                value = d.avgResponse.label,
                icon = Icons.Default.Schedule,
                accent = VipDeskOrange,
                trend = d.avgResponse.deltaLabel,
                trendPositive = d.avgResponse.improved ?: true
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Agentes online",
                value = d.agentsOnline.value.toString(),
                icon = Icons.Default.People,
                accent = VipDeskGreen,
                trend = d.agentsOnline.status,
                trendPositive = d.agentsOnline.value > 0
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent tickets
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tickets recentes",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (uiState.isLoading || uiState.isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = VipDeskPurple,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Ver todos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VipDeskPurple,
                    modifier = Modifier.clickable { onSeeAllTickets() }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionCard(
            modifier = Modifier.fillMaxWidth(),
            padding = PaddingValues(vertical = 6.dp)
        ) {
            Column {
                when {
                    uiState.error != null && d.recentTickets.isEmpty() -> {
                        EmptyHint(uiState.error ?: "Erro ao carregar")
                    }
                    d.recentTickets.isEmpty() && !uiState.isLoading -> {
                        EmptyHint("Nenhum ticket aberto no momento")
                    }
                    else -> {
                        d.recentTickets.forEachIndexed { index, t ->
                            RecentTicketRow(t) { onTicketClick(t.id) }
                            if (index < d.recentTickets.lastIndex) {
                                HorizontalDivider(
                                    color = c.divider,
                                    modifier = Modifier.padding(start = 70.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    trend: String?,
    trendPositive: Boolean
) {
    val c = AppTheme.colors
    SectionCard(modifier = modifier.height(124.dp), padding = PaddingValues(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(accent.copy(alpha = if (c.isDark) 0.24f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                color = c.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (!trend.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (trendPositive) Icons.Default.TrendingUp
                        else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (trendPositive) VipDeskGreen else VipDeskRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = trend,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (trendPositive) VipDeskGreen else VipDeskRed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTicketRow(t: RecentTicket, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (prioLabel, prioColor) = ticketPriorityVisual(t.priority)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PurpleGradientLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(t.contactName),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = t.contactName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = t.title?.takeIf { it.isNotBlank() } ?: (t.ticketNumber ?: "Ticket"),
                fontSize = 12.sp,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            t.updatedAt?.let {
                Text(text = relativeTime(it), fontSize = 11.sp, color = c.textSecondary)
            }
            Spacer(modifier = Modifier.height(5.dp))
            StatusBadge(label = prioLabel, color = prioColor)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = AppTheme.colors.textSecondary, fontSize = 13.sp)
    }
}
