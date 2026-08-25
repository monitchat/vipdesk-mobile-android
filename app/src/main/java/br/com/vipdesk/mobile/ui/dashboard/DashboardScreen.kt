package br.com.vipdesk.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.RecentTicket
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdIconButton
import br.com.vipdesk.mobile.ui.components.VdKpiCard
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.VdWarning
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onTicketClick: (Int) -> Unit,
    onSeeAllTickets: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val dash = state.dashboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.clip(CircleShape).clickable(onClick = onSettingsClick)) {
                VdAvatar(name = state.userName.ifBlank { "A" }, size = 40.dp, fontSize = 15)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "${greeting()}, ${state.userName.ifBlank { "agente" }}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    color = c.textPrimary
                )
                Text(todayLabel(), fontSize = 12.sp, color = c.textSecondary)
            }
            VdIconButton(Icons.Outlined.Search, onClick = onSearchClick)
            VdIconButton(
                Icons.Outlined.Notifications,
                onClick = onNotificationsClick,
                showDot = state.unreadNotifications > 0
            )
        }

        if (state.isLoading && dash.recentTickets.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(0.dp))

            // Alerta de SLA
            if (!dash.sla.within) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VdDanger.copy(alpha = 0.10f))
                        .border(1.dp, VdDanger.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onSeeAllTickets)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = VdDanger, modifier = Modifier.size(18.dp))
                    Text(
                        buildString {
                            append("SLA em risco")
                            if (dash.sla.status.isNotBlank()) append(" · ${dash.sla.status}")
                        },
                        fontSize = 13.sp,
                        color = c.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                        tint = c.textSecondary, modifier = Modifier.size(18.dp)
                    )
                }
            }

            // KPIs (2 colunas)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VdKpiCard(
                    number = "${dash.ticketsOpen.value}",
                    label = "Tickets abertos",
                    icon = Icons.Outlined.ConfirmationNumber,
                    delta = dash.ticketsOpen.deltaLabel,
                    deltaColor = if (dash.ticketsOpen.deltaPct <= 0) VdSuccess else VdWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onSeeAllTickets
                )
                VdKpiCard(
                    number = "${dash.sla.value}${dash.sla.unit}",
                    label = "SLA cumprido",
                    icon = Icons.Outlined.Verified,
                    numberColor = if (dash.sla.within) c.textPrimary else VdDanger,
                    modifier = Modifier.weight(1f),
                    onClick = onSeeAllTickets
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VdKpiCard(
                    number = dash.avgResponse.label,
                    label = "Tempo médio de resposta",
                    icon = Icons.Outlined.Timer,
                    delta = dash.avgResponse.deltaLabel,
                    deltaColor = if (dash.avgResponse.improved == true) VdSuccess else VdWarning,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenInbox
                )
                VdKpiCard(
                    number = "${dash.agentsOnline.value}",
                    label = "Agentes ativos",
                    icon = Icons.Outlined.Groups,
                    modifier = Modifier.weight(1f)
                )
            }

            // Atividade recente (tickets)
            VdCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VdSectionLabel("Atividade recente")
                    Spacer(Modifier.weight(1f))
                    Text(
                        "ver tudo",
                        fontSize = 12.sp,
                        color = c.accent,
                        modifier = Modifier.clickable(onClick = onSeeAllTickets)
                    )
                }
                if (dash.recentTickets.isEmpty()) {
                    Text(
                        "Nenhum ticket recente.",
                        fontSize = 13.sp,
                        color = c.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    dash.recentTickets.forEachIndexed { i, t ->
                        RecentTicketRow(
                            ticket = t,
                            showDivider = i < dash.recentTickets.lastIndex,
                            onClick = { onTicketClick(t.id) }
                        )
                    }
                }
            }

            state.error?.let {
                Text(it, fontSize = 12.sp, color = VdDanger)
            }

            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
private fun RecentTicketRow(
    ticket: RecentTicket,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    val (stLabel, stColor) = ticketStatusVisual(ticket.status, null)
    val (_, priColor) = ticketPriorityVisual(ticket.priority)
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(c.chip, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsOf(ticket.contactName),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textSecondary
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    ticket.title ?: "Ticket ${ticket.ticketNumber ?: "#${ticket.id}"}",
                    fontSize = 13.5.sp,
                    color = c.textPrimary,
                    maxLines = 2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Box(Modifier.size(6.dp).background(priColor, CircleShape))
                    VdTag(stLabel, color = stColor, background = stColor.copy(alpha = 0.15f))
                    Text(
                        buildString {
                            append(ticket.contactName)
                            ticket.createdAt?.let { append(" · ${relativeTime(it)}") }
                        },
                        fontSize = 11.5.sp,
                        color = c.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
        if (showDivider) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(c.divider)
            )
        }
    }
}

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Bom dia"
        h < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
}

private fun todayLabel(): String {
    val fmt = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
    return fmt.format(Date()).replaceFirstChar { it.uppercase() }
}
