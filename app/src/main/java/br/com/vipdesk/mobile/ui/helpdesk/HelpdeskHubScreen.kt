package br.com.vipdesk.mobile.ui.helpdesk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Approval
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkHistory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.MobileDashboard
import br.com.vipdesk.mobile.data.model.StatisticsResponse
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAppHeader
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint


/** Hub do Helpdesk (tela 13): KPIs, grade de operação e lista de gestão. */
@Composable
fun HelpdeskHubScreen(
    onOpenTickets: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenReports: () -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    onCreateTicket: () -> Unit,
    onToast: (String) -> Unit,
    unreadNotifications: Int = 0,
    onOpenModule: (String) -> Unit = {}
) {
    val c = AppTheme.colors
    // null = ainda não carregou (ou falhou): os KPIs mostram "—" em vez de zeros falsos.
    var dash by remember { mutableStateOf<MobileDashboard?>(null) }
    var stats by remember { mutableStateOf<StatisticsResponse?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { if (br.com.vipdesk.mobile.ui.kanban.KanbanStore.boards.isEmpty()) br.com.vipdesk.mobile.ui.kanban.KanbanStore.load() }
    LaunchedEffect(reloadKey) {
        loadError = null
        AppContainer.mobileRepository.getDashboard().fold(
            onSuccess = { dash = it },
            onFailure = { loadError = it.message ?: "Falha ao carregar indicadores" }
        )
        AppContainer.mobileRepository.getStatistics("today").fold(
            onSuccess = { stats = it },
            onFailure = { loadError = it.message ?: "Falha ao carregar indicadores" }
        )
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdAppHeader(title = "Helpdesk", presence = null) {
            VdHeaderIcon(Icons.Outlined.Search, "Buscar", onSearch)
            VdHeaderIcon(Icons.Outlined.Notifications, "Notificações", onNotifications, badge = unreadNotifications)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            loadError?.let { msg ->
                VdCard(padding = 12.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(msg, fontSize = 12.sp, color = Tint.amberFg, modifier = Modifier.weight(1f))
                        Text(
                            "Tentar novamente", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.primary,
                            modifier = Modifier.clickable { reloadKey++ }
                        )
                    }
                }
            }

            // KPIs de hoje
            VdCard(padding = 12.dp) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val sla = dash?.sla
                    Kpi(
                        "SLA 30 dias", sla?.let { "${it.value}${it.unit}" } ?: "—",
                        sub = when {
                            sla == null -> "meta 90%"
                            sla.within -> "▲ na meta ${sla.target}%"
                            else -> "▼ meta ${sla.target}%"
                        },
                        subColor = if (sla?.within == true) Tint.greenFg else Tint.amberFg,
                        modifier = Modifier.weight(1f)
                    )
                    Kpi("Abertos hoje", stats?.openTickets?.count?.toString() ?: "—", sub = "${stats?.waitingTickets?.count ?: 0} aguardando", modifier = Modifier.weight(1f))
                    Kpi("Finalizados", stats?.closedTickets?.count?.toString() ?: "—", sub = "hoje", modifier = Modifier.weight(1f))
                }
            }

            VdSectionLabel("Operação")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.ConfirmationNumber, "Tickets",
                    "${dash?.ticketsOpen?.value ?: "—"} abertos · ${stats?.assignedTickets?.count ?: "—"} em atendimento",
                    badge = stats?.ignoredTickets?.count?.takeIf { it > 0 }?.let { "$it sem resp." },
                    modifier = Modifier.weight(1f), onClick = onOpenTickets)
                OpCard(Icons.Outlined.CheckBox, "Minhas tarefas", "quadro ${br.com.vipdesk.mobile.ui.kanban.KanbanStore.boardName}", modifier = Modifier.weight(1f), onClick = onOpenKanban)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.ViewKanban, "Boards Kanban", "${br.com.vipdesk.mobile.ui.kanban.KanbanStore.boards.size} board(s)", modifier = Modifier.weight(1f), onClick = onOpenKanban)
                OpCard(Icons.Outlined.Phone, "Ligações", "histórico · gravações", modifier = Modifier.weight(1f)) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.CALLS) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.MenuBook, "Base de Conhecimento", "artigos", modifier = Modifier.weight(1f)) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.KB) }
                OpCard(Icons.Outlined.Approval, "Aprovações", "aguardando você", modifier = Modifier.weight(1f)) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.APPROVALS) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.Devices, "Inventário", "ativos · licenças · CMDB", modifier = Modifier.weight(1f)) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.ASSETS) }
                OpCard(Icons.Outlined.WorkHistory, "Aprovar horas", "worklogs da equipe", modifier = Modifier.weight(1f)) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.HOURS) }
            }

            VdSectionLabel("Gestão")
            VdCard(padding = 0.dp) {
                ManageRow(Icons.Outlined.Speed, "Dashboard", onClick = onOpenDashboard)
                ManageRow(Icons.Outlined.BarChart, "Relatórios de service desk", onClick = onOpenReports)
                ManageRow(Icons.Outlined.Mood, "Análise de sentimento (XLA)", tag = "IA") { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.SENTIMENT) }
                ManageRow(Icons.Outlined.Timer, "SLA por prioridade · Categorias") { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.SLA) }
                ManageRow(Icons.Outlined.Work, "Projetos", last = true) { onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.PROJECTS) }
            }

            // Atalho: criar ticket
            Text(
                "+ Criar ticket",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.primary,
                modifier = Modifier.clickable(onClick = onCreateTicket).padding(vertical = 4.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Kpi(label: String, value: String, sub: String, modifier: Modifier = Modifier, subColor: Color? = null) {
    val c = AppTheme.colors
    Column(modifier) {
        Text(label.uppercase(), fontSize = 10.sp, letterSpacing = 0.4.sp, color = c.muted, maxLines = 1)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = c.text, lineHeight = 26.sp)
        Text(sub, fontSize = 11.sp, color = subColor ?: c.muted, maxLines = 1)
    }
}

@Composable
private fun OpCard(
    icon: ImageVector, title: String, subtitle: String,
    modifier: Modifier = Modifier, badge: String? = null, onClick: () -> Unit
) {
    val c = AppTheme.colors
    Box(modifier) {
        VdCard(padding = 12.dp, onClick = onClick, modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 92.dp)) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(subtitle, fontSize = 11.sp, color = c.muted, maxLines = 2)
        }
        if (badge != null) {
            Text(
                badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .background(c.danger, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun ManageRow(icon: ImageVector, label: String, tag: String? = null, last: Boolean = false, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).defaultMinSize(minHeight = 46.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 13.sp, color = c.text, modifier = Modifier.weight(1f))
        if (tag != null) {
            Text(tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = c.primary,
                modifier = Modifier.background(c.primarySurface, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 1.dp))
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(18.dp))
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}
