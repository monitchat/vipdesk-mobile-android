package br.com.vipdesk.mobile.ui.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
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
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.components.VdBar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdKpiCard
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdPresenceChip
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusTint
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint

private val PERIODS = listOf("Hoje" to "today", "Ontem" to "yesterday", "Semana" to "week", "Mês" to "month")

/** Dashboard operacional (tela 08): grade 4×2 de KPIs, por canal, atividade recente. */
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    onTicketClick: (Int) -> Unit,
    onSeeAllTickets: () -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val dash = state.dashboard
    val stats = state.stats

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(
            title = "Dashboard",
            onBack = onBack,
            actions = {
                br.com.vipdesk.mobile.ui.components.VdLivePresenceChip()
                VdHeaderIcon(Icons.Outlined.Notifications, "Notificações", onNotificationsClick, badge = state.unreadNotifications)
            }
        )
        VdPillRow(modifier = Modifier.padding(vertical = 10.dp)) {
            PERIODS.forEach { (label, key) -> VdPill(label, state.period == key, { viewModel.setPeriod(key) }) }
        }

        if (state.isLoading && dash.recentTickets.isEmpty() && stats.openTickets.count == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            return@Column
        }

        br.com.vipdesk.mobile.ui.components.VdPullRefresh(onRefresh = { viewModel.refreshAwait() }) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Grade 4×2
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VdKpiCard("${stats.waitingTickets.count}", "Em fila", Modifier.weight(1f), numberColor = if (stats.waitingTickets.count > 0) Tint.amberFg else c.text, onClick = onSeeAllTickets)
                VdKpiCard("${stats.assignedTickets.count}", "Em atend.", Modifier.weight(1f), onClick = onSeeAllTickets)
                VdKpiCard("${stats.ignoredTickets.count}", "Sem resp.", Modifier.weight(1f), numberColor = if (stats.ignoredTickets.count > 0) Tint.redFg else c.text, delta = "> timeout", onClick = onSeeAllTickets)
                VdKpiCard("${stats.closedTickets.count}", "Finaliz.", Modifier.weight(1f), deltaColor = Tint.greenFg, delta = periodLabel(state.period), onClick = onSeeAllTickets)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VdKpiCard(
                    "${dash.sla.value}%", "SLA 30d", Modifier.weight(1f),
                    numberColor = if (dash.sla.within) c.text else Tint.amberFg,
                    delta = "meta ${dash.sla.target}%", deltaColor = if (dash.sla.within) Tint.greenFg else Tint.redFg,
                    topAccent = if (dash.sla.within) null else c.warning
                )
                VdKpiCard(dash.avgResponse.label, "TME", Modifier.weight(1f), delta = dash.avgResponse.deltaLabel, deltaColor = if (dash.avgResponse.improved == true) Tint.greenFg else Tint.redFg)
                VdKpiCard("${stats.openTickets.count}", "Iniciados", Modifier.weight(1f), delta = periodLabel(state.period), onClick = onSeeAllTickets)
                VdKpiCard("${dash.agentsOnline.value}", "Online", Modifier.weight(1f), delta = if (dash.agentsOnline.value > 0) "agentes" else "ninguém", deltaColor = if (dash.agentsOnline.value > 0) Tint.greenFg else c.muted)
            }

            // Termômetro (distribuição por status)
            val statuses = state.distributions.status.filter { it.total > 0 }
            if (statuses.isNotEmpty()) {
                val total = statuses.sumOf { it.total }.coerceAtLeast(1)
                VdCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tickets por status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                        Text("$total tickets", fontSize = 11.sp, color = c.muted)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
                        statuses.forEachIndexed { i, s ->
                            val (_, fg) = ticketStatusTint(s.label)
                            Box(Modifier.weight(s.total.toFloat()).height(14.dp).background(statusBar(fg, i)), contentAlignment = Alignment.Center) {
                                if (s.total * 100 / total >= 12) Text("${s.total}", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        statuses.forEachIndexed { i, s ->
                            val (_, fg) = ticketStatusTint(s.label)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(7.dp).background(statusBar(fg, i), RoundedCornerShape(2.dp)))
                                Text("${s.label} · ${s.total} (${s.total * 100 / total}%)", fontSize = 10.sp, color = c.muted)
                            }
                        }
                    }
                }
            }

            // Por canal
            val sources = state.distributions.source.filter { it.total > 0 }.sortedByDescending { it.total }
            if (sources.isNotEmpty()) {
                val total = sources.sumOf { it.total }.coerceAtLeast(1)
                VdCard(padding = 12.dp) {
                    Text("Por canal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                    sources.forEach { s ->
                        val sv = sourceVisual(s.source)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                            Icon(sv.icon, null, tint = sv.color, modifier = Modifier.size(16.dp))
                            Text(sourceLabel(s.source), fontSize = 11.sp, color = c.text, modifier = Modifier.width(64.dp), maxLines = 1)
                            VdBar(s.total.toFloat() / total, c.primary, Modifier.weight(1f))
                            Text("${s.total * 100 / total}%", fontSize = 11.sp, color = c.muted, modifier = Modifier.width(30.dp))
                        }
                    }
                }
            }

            // Por departamento
            val depts = state.distributions.departments.filter { it.total > 0 }.sortedByDescending { it.total }
            if (depts.isNotEmpty()) {
                val max = depts.maxOf { it.total }
                VdCard(padding = 12.dp) {
                    Text("Por departamento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                    depts.take(6).forEach { d ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(d.label, fontSize = 11.sp, color = c.text, modifier = Modifier.width(90.dp), maxLines = 1)
                            VdBar(d.total.toFloat() / max, c.primaryLight, Modifier.weight(1f))
                            Text("${d.total}", fontSize = 11.sp, color = c.muted, modifier = Modifier.width(30.dp))
                        }
                    }
                }
            }

            // Atividade recente
            VdCard(padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("Tickets recentes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                    Text("ver todos", fontSize = 11.sp, color = c.primary, modifier = Modifier.clickable(onClick = onSeeAllTickets))
                }
                if (dash.recentTickets.isEmpty()) Text("Nenhum ticket recente.", fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(vertical = 8.dp))
                dash.recentTickets.forEachIndexed { i, t ->
                    val (stLabel, _) = ticketStatusVisual(t.status, null)
                    val (bg, fg) = ticketStatusTint(stLabel)
                    val (_, priColor) = ticketPriorityVisual(t.priority)
                    Row(
                        Modifier.fillMaxWidth().clickable { onTicketClick(t.id) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(28.dp).background(c.surfaceAlt, CircleShape), contentAlignment = Alignment.Center) {
                            Text(initialsOf(t.contactName), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF374151))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(t.title ?: "Ticket #${t.id}", fontSize = 12.sp, color = c.text, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(6.dp).background(priColor, CircleShape))
                                Text(listOfNotNull(t.contactName, t.createdAt?.let { relativeTime(it) }).joinToString(" · "), fontSize = 10.sp, color = c.muted, maxLines = 1)
                            }
                        }
                        VdTag(stLabel, color = fg, background = bg)
                    }
                    if (i < dash.recentTickets.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
                }
            }
            state.error?.let { Text(it, fontSize = 12.sp, color = c.danger) }
            Spacer(Modifier.height(24.dp))
        }
        }
    }
}

private fun statusBar(fg: Color, index: Int): Color {
    val palette = listOf(Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF22C55E), Color(0xFFEF4444), Color(0xFF7E3E97), Color(0xFF9CA3AF))
    return when (fg) {
        Tint.blueFg -> palette[0]; Tint.yellowFg -> palette[1]; Tint.greenFg -> palette[2]
        Tint.redFg -> palette[3]; Color(0xFF7E3E97) -> palette[4]
        else -> palette[index % palette.size]
    }
}

private fun periodLabel(period: String) = when (period) {
    "today" -> "hoje"; "yesterday" -> "ontem"; "week" -> "semana"; "month" -> "mês"; else -> period
}
