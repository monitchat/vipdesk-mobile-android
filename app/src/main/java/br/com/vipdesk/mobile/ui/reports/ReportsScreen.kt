package br.com.vipdesk.mobile.ui.reports

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.StatisticsCountResponse
import br.com.vipdesk.mobile.data.model.StatisticsResponse
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdKpiCard
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.modules.apiGet
import br.com.vipdesk.mobile.ui.modules.int
import br.com.vipdesk.mobile.ui.modules.rows
import br.com.vipdesk.mobile.ui.modules.str
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonObject

private val PERIODS = listOf("Hoje" to "today", "Ontem" to "yesterday", "Semana" to "week", "Mês" to "month")

/** Relatórios de atendimento (tela 29): mesmos endpoints do dashboard web. */
@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val c = AppTheme.colors
    val context = LocalContext.current
    var period by remember { mutableStateOf("month") }
    var stats by remember { mutableStateOf<StatisticsResponse?>(null) }
    var counts by remember { mutableStateOf<StatisticsCountResponse?>(null) }
    var agents by remember { mutableStateOf<List<JsonObject>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableStateOf(0) }

    LaunchedEffect(period, reloadTick) {
        stats = null; counts = null; agents = null; error = null
        AppContainer.mobileRepository.getStatistics(period).fold({ stats = it }, { error = it.message })
        AppContainer.mobileRepository.getStatisticsCount(period).onSuccess { counts = it }
        val filter = """{"created":"$period","by":"company","user_id":null,"current_status":null,"department_id":null,"start":null,"end":null,"start_date":null,"end_date":null}"""
        apiGet("statistic/ticketsDashboardTable", mapOf("filter" to filter, "take" to "100", "no_cache" to "1")).fold(
            { agents = it.rows().first }, { agents = emptyList() }
        )
    }

    fun exportCsv() {
        val s = stats ?: return
        val label = PERIODS.first { it.second == period }.first
        val sb = StringBuilder("Relatório VIPdesk;$label\n\nIndicador;Quantidade\n")
        sb.append("Em aberto;${s.openTickets.count}\nEm atendimento;${s.assignedTickets.count}\nAguardando;${s.waitingTickets.count}\nSem resposta;${s.ignoredTickets.count}\nFinalizados;${s.closedTickets.count}\n")
        agents?.takeIf { it.isNotEmpty() }?.let { list ->
            sb.append("\nAgente;Aguardando;Em aberto;Finalizados\n")
            list.forEach { a -> sb.append("${a.str("user")};${a.int("waitingTicket") ?: 0};${a.int("openTickets") ?: 0};${a.int("closedTickets") ?: 0}\n") }
        }
        counts?.departments?.takeIf { it.isNotEmpty() }?.let { d -> sb.append("\nDepartamento;Tickets\n"); d.forEach { sb.append("${it.label};${it.total}\n") } }
        counts?.source?.takeIf { it.isNotEmpty() }?.let { d -> sb.append("\nCanal;Tickets\n"); d.forEach { sb.append("${it.label};${it.total}\n") } }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Relatório VIPdesk · $label")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Exportar relatório")) }
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(
            title = "Relatórios", subtitle = "Atendimento e tickets", onBack = onBack,
            actions = { VdHeaderIcon(Icons.Outlined.IosShare, "Exportar", { exportCsv() }) },
            below = { VdPillRow(contentPaddingStart = 0.dp) { PERIODS.forEach { (l, v) -> VdPill(l, period == v, { period = v }) } } }
        )
        when {
            error != null && stats == null -> VdEmptyState(Icons.Outlined.BarChart, "Não foi possível carregar", error ?: "")
            stats == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            else -> br.com.vipdesk.mobile.ui.components.VdPullRefresh(onRefresh = { reloadTick++; kotlinx.coroutines.delay(600) }) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val s = stats!!
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${s.openTickets.count}", "EM ABERTO", modifier = Modifier.weight(1f), topAccent = Tint.blueFg)
                    VdKpiCard("${s.assignedTickets.count}", "EM ATENDIMENTO", modifier = Modifier.weight(1f), topAccent = c.primary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${s.waitingTickets.count}", "AGUARDANDO", modifier = Modifier.weight(1f), topAccent = Tint.amberFg)
                    VdKpiCard("${s.ignoredTickets.count}", "SEM RESPOSTA", modifier = Modifier.weight(1f), topAccent = Tint.redFg)
                    VdKpiCard("${s.closedTickets.count}", "FINALIZADOS", modifier = Modifier.weight(1f), topAccent = Tint.greenFg)
                }

                VdSectionLabel("Desempenho por agente")
                when {
                    agents == null -> CircularProgressIndicator(color = c.primary, modifier = Modifier.padding(8.dp))
                    agents!!.isEmpty() -> Text("Sem dados de agentes no período.", fontSize = 12.sp, color = c.muted)
                    else -> VdCard {
                        val list = agents!!.sortedByDescending { it.int("closedTickets") ?: 0 }
                        val max = list.maxOfOrNull { (it.int("closedTickets") ?: 0) + (it.int("openTickets") ?: 0) }?.coerceAtLeast(1) ?: 1
                        list.forEach { a ->
                            val closed = a.int("closedTickets") ?: 0; val open = a.int("openTickets") ?: 0; val waiting = a.int("waitingTicket") ?: 0
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Row {
                                    Text(a.str("user") ?: "—", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("$closed finalizados · $open abertos" + (if (waiting > 0) " · $waiting aguard." else ""), fontSize = 11.sp, color = c.muted)
                                }
                                Row(Modifier.fillMaxWidth().padding(top = 4.dp).height(8.dp).background(c.surfaceAlt, RoundedCornerShape(4.dp))) {
                                    if (closed > 0) Box(Modifier.fillMaxHeight().weight(closed.toFloat()).background(Tint.greenFg, RoundedCornerShape(4.dp)))
                                    if (open > 0) Box(Modifier.fillMaxHeight().weight(open.toFloat()).background(Tint.blueFg, RoundedCornerShape(4.dp)))
                                    val rest = max - closed - open
                                    if (rest > 0) Spacer(Modifier.fillMaxHeight().weight(rest.toFloat()))
                                }
                            }
                        }
                    }
                }

                counts?.let { ct ->
                    if (ct.departments.isNotEmpty()) { VdSectionLabel("Por departamento"); Bars(ct.departments.map { it.label to it.total }) }
                    if (ct.source.isNotEmpty()) { VdSectionLabel("Por canal"); Bars(ct.source.map { it.label to it.total }) }
                    if (ct.status.isNotEmpty()) { VdSectionLabel("Por status"); Bars(ct.status.map { it.label to it.total }) }
                }
                Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun Bars(data: List<Pair<String, Int>>) {
    val c = AppTheme.colors
    VdCard {
        val max = data.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
        data.sortedByDescending { it.second }.forEach { (label, v) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 12.sp, color = c.text, modifier = Modifier.width(120.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box(Modifier.weight(1f).height(10.dp).background(c.surfaceAlt, RoundedCornerShape(5.dp))) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(v.toFloat() / max).background(c.primary, RoundedCornerShape(5.dp)))
                }
                Text("$v", fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
