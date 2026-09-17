package br.com.vipdesk.mobile.ui.crm

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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneForwarded
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAppHeader
import br.com.vipdesk.mobile.ui.components.VdBar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val WEB_ONLY = "Disponível na versão web"

/** Hub do CRM "Vendas" (tela 16). */
@Composable
fun CrmHubScreen(
    onOpenDeals: () -> Unit,
    onOpenContacts: () -> Unit,
    onOpenKanban: () -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    onToast: (String) -> Unit,
    unreadNotifications: Int = 0
) {
    val c = AppTheme.colors
    var openDeals by remember { mutableStateOf<List<ApiDeal>>(emptyList()) }
    var wonMonth by remember { mutableStateOf<List<ApiDeal>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(CrmEvents.dealsVersion) {
        AppContainer.crmRepository.listDeals(null, "open", kanban = true).fold(
            onSuccess = { openDeals = it; error = null },
            onFailure = { error = it.message }
        )
        AppContainer.crmRepository.listDeals(null, "won").onSuccess { list ->
            val cal = Calendar.getInstance()
            val month = cal.get(Calendar.MONTH); val year = cal.get(Calendar.YEAR)
            wonMonth = list.filter { d ->
                val date = (d.closedAt ?: d.updatedAt)?.let { parseDate(it) } ?: return@filter false
                val dc = Calendar.getInstance().apply { time = date }
                dc.get(Calendar.MONTH) == month && dc.get(Calendar.YEAR) == year
            }
        }
    }

    val openSum = openDeals.sumOf { it.value ?: 0.0 }
    val wonSum = wonMonth.sumOf { it.value ?: 0.0 }
    val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdAppHeader(title = "Vendas", presence = null) {
            VdHeaderIcon(Icons.Outlined.Search, "Buscar", onSearch)
            VdHeaderIcon(Icons.Outlined.Notifications, "Notificações", onNotifications, badge = unreadNotifications)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Meu mês
            VdCard(padding = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MEU MÊS · ${monthLabel()}", fontSize = 11.sp, letterSpacing = 0.4.sp, color = c.muted, modifier = Modifier.weight(1f))
                    Text("dia $day de $daysInMonth", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tint.greenFg)
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(formatDealValue(wonSum, "BRL").takeIf { wonSum > 0 } ?: "R$ 0", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                    Text("ganhos · ${wonMonth.size} negócio${if (wonMonth.size == 1) "" else "s"}", fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(bottom = 3.dp))
                }
                Spacer(Modifier.height(8.dp))
                VdBar(fraction = day.toFloat() / daysInMonth, color = c.primary, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(4.dp))
                Row {
                    Text("Em aberto: ${formatDealValue(openSum, "BRL").takeIf { openSum > 0 } ?: "R$ 0"}", fontSize = 10.sp, color = c.muted, modifier = Modifier.weight(1f))
                    Text("${openDeals.size} negócios", fontSize = 10.sp, color = c.muted)
                }
            }
            error?.let { Text(it, fontSize = 12.sp, color = c.danger) }

            VdSectionLabel("Operação")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.ViewKanban, "Negócios", "${openDeals.size} abertos · ${formatDealValue(openSum, "BRL").takeIf { openSum > 0 } ?: "R$ 0"}", Modifier.weight(1f), onOpenDeals)
                OpCard(Icons.Outlined.CheckBox, "Minhas tarefas", "quadro ${br.com.vipdesk.mobile.ui.kanban.KanbanStore.boardName}", Modifier.weight(1f), onOpenKanban)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.Contacts, "Relacionamento", "contatos · clientes · grupos", Modifier.weight(1f), onOpenContacts)
                OpCard(Icons.Outlined.Repeat, "Cadências", "toques pendentes", Modifier.weight(1f)) { onToast(WEB_ONLY) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OpCard(Icons.Outlined.PhoneForwarded, "Discador", "campanhas de ligação", Modifier.weight(1f)) { onToast(WEB_ONLY) }
                OpCard(Icons.Outlined.Description, "Orçamentos e contratos", "aguardando assinatura", Modifier.weight(1f)) { onToast(WEB_ONLY) }
            }

            VdSectionLabel("Dashboards · SDR · CS · Configuração")
            VdCard(padding = 0.dp) {
                ManageRow(Icons.AutoMirrored.Outlined.TrendingUp, "Dashboards (vendas, por vendedor, gerencial)") { onToast(WEB_ONLY) }
                ManageRow(Icons.Outlined.TrackChanges, "SDR · agendamentos, performance, metas") { onToast(WEB_ONLY) }
                ManageRow(Icons.Outlined.MonitorHeart, "Customer Success · saúde da carteira") { onToast(WEB_ONLY) }
                ManageRow(Icons.Outlined.Language, "Landing pages · Link na bio") { onToast(WEB_ONLY) }
                ManageRow(Icons.Outlined.EmojiEvents, "Metas · Comissões · Lead scoring") { onToast(WEB_ONLY) }
                ManageRow(Icons.Outlined.Settings, "Pipelines · Produtos · Motivos de perda", last = true) { onToast(WEB_ONLY) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OpCard(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    VdCard(padding = 12.dp, onClick = onClick, modifier = modifier.defaultMinSize(minHeight = 88.dp)) {
        Icon(icon, null, tint = c.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        Text(subtitle, fontSize = 11.sp, color = c.muted, maxLines = 2)
    }
}

@Composable
private fun ManageRow(icon: ImageVector, label: String, last: Boolean = false, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).defaultMinSize(minHeight = 44.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = c.textTertiary, modifier = Modifier.size(20.dp))
        Text(label, fontSize = 13.sp, color = c.text, modifier = Modifier.weight(1f), maxLines = 1)
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(18.dp))
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}

private fun monthLabel(): String =
    SimpleDateFormat("MMM/yyyy", Locale("pt", "BR")).format(Calendar.getInstance().time).replaceFirstChar { it.uppercase() }

private fun parseDate(s: String): java.util.Date? = br.com.vipdesk.mobile.ui.common.parseApiDate(s)
