package br.com.vipdesk.mobile.ui.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.demo.DEMO_AGENT_PERF
import br.com.vipdesk.mobile.data.demo.DEMO_DEPT_PERF
import br.com.vipdesk.mobile.data.demo.DEMO_REPORT_BARS
import br.com.vipdesk.mobile.data.demo.DEMO_REPORT_KPIS
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdBar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdKpiCard
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

private val PERIODS = listOf("Hoje", "Ontem", "7 dias", "30 dias", "Período…")

@Composable
fun ReportsScreen(onBack: () -> Unit) {
    val c = AppTheme.colors
    var period by remember { mutableStateOf("7 dias") }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                }
                Text(
                    "Relatórios",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { toast = "Relatório exportado (PDF)" }) {
                    Icon(
                        Icons.Outlined.IosShare, "Exportar",
                        tint = c.textSecondary, modifier = Modifier.size(19.dp)
                    )
                }
            }

            VdPillRow(modifier = Modifier.padding(vertical = 10.dp)) {
                PERIODS.forEach { p ->
                    VdPill(label = p, selected = period == p, onClick = { period = p })
                }
                VdPill(label = "Equipe · Canal", selected = false, onClick = { })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // KPIs
                DEMO_REPORT_KPIS.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { kpi ->
                            VdKpiCard(
                                number = kpi.n,
                                label = kpi.label,
                                delta = kpi.delta,
                                deltaColor = if (kpi.positive) VdSuccess else VdDanger,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Conversas ao longo do tempo
                VdCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VdSectionLabel("Conversas ao longo do tempo")
                        Spacer(Modifier.weight(1f))
                        Text(period, fontSize = 11.sp, color = c.textFaint)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        DEMO_REPORT_BARS.forEach { v ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height((v * 0.8f * 0.88f).dp)
                                        .background(
                                            if (c.isDark) Color(0xFF5D5294) else c.accent,
                                            RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                        )
                                )
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height((v * 0.12f * 0.88f).dp)
                                        .background(
                                            c.chip,
                                            RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
                                        )
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Legend(if (c.isDark) Color(0xFF5D5294) else c.accent, "Atendidas")
                        Legend(c.chip, "Perdidas")
                    }
                }

                // Satisfação
                VdCard {
                    VdSectionLabel("Satisfação do cliente", Modifier.padding(bottom = 12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(Modifier.size(92.dp)) {
                                val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
                                drawArc(
                                    color = Color(0x22E9E9ED),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = stroke,
                                    size = Size(size.width, size.height)
                                )
                                drawArc(
                                    color = VdSuccess,
                                    startAngle = -90f,
                                    sweepAngle = 338f,
                                    useCenter = false,
                                    style = stroke,
                                    size = Size(size.width, size.height)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "94%",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = c.textPrimary
                                )
                                Text("CSAT", fontSize = 9.5.sp, color = c.textSecondary)
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            StatLine("Avaliações", "312")
                            StatLine("Nota média", "4,8 / 5")
                            StatLine("NPS", "+72", VdSuccess)
                        }
                    }
                }

                // Desempenho por agente
                VdCard {
                    VdSectionLabel("Desempenho por agente", Modifier.padding(bottom = 10.dp))
                    val max = DEMO_AGENT_PERF.maxOf { it.value }
                    DEMO_AGENT_PERF.forEach { agent ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 7.dp)
                        ) {
                            VdAvatar(name = agent.name, size = 28.dp, fontSize = 10)
                            Text(
                                agent.name,
                                fontSize = 13.sp,
                                color = c.textPrimary,
                                modifier = Modifier.width(104.dp)
                            )
                            VdBar(
                                fraction = agent.value.toFloat() / max,
                                color = if (c.isDark) Color(0xFF796CBF) else c.accent,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${agent.value}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary,
                                modifier = Modifier.width(30.dp)
                            )
                        }
                    }
                }

                // Tickets por departamento
                VdCard {
                    VdSectionLabel("Tickets por departamento", Modifier.padding(bottom = 10.dp))
                    val max = DEMO_DEPT_PERF.maxOf { it.value }
                    DEMO_DEPT_PERF.forEach { dept ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                dept.name,
                                fontSize = 12.5.sp,
                                color = c.textSecondary,
                                modifier = Modifier.width(104.dp)
                            )
                            VdBar(
                                fraction = dept.value.toFloat() / max,
                                color = Color(dept.colorHex),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${dept.value}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary,
                                modifier = Modifier.width(30.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        toast?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) { VdToast(it) }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    val c = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, fontSize = 11.sp, color = c.textSecondary)
    }
}

@Composable
private fun StatLine(label: String, value: String, valueColor: Color? = null) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.5.sp, color = c.textSecondary, modifier = Modifier.weight(1f))
        Text(
            value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor ?: c.textPrimary
        )
    }
}
