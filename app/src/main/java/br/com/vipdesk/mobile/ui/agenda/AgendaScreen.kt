package br.com.vipdesk.mobile.ui.agenda

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.Appointment
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAppHeader
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val WEB_ONLY = "Disponível na versão web"
private val ISO = "yyyy-MM-dd"

/** Visual do status do agendamento: (fundo, borda, rótulo, texto). */
private fun statusVisual(status: String?): Quad {
    val c = status?.lowercase()
    return when (c) {
        "confirmed" -> Quad(Tint.greenBg, Color(0xFF22C55E), "Confirmado ✓", Tint.greenFg)
        "pending" -> Quad(Tint.yellowBg, Color(0xFFF59E0B), "Aguard. confirmação", Tint.yellowFg)
        "arrived", "in_service" -> Quad(Tint.blueBg, Color(0xFF3B82F6), if (c == "arrived") "Chegou" else "Em atendimento", Tint.blueFg)
        "completed" -> Quad(Color(0xFFE5E7EB), Color(0xFF4B5563), "Realizado", Color(0xFF374151))
        "canceled", "no_show" -> Quad(Color(0xFFF3F4F6), Color(0xFF9CA3AF), if (c == "canceled") "Cancelado" else "Não compareceu", Color(0xFF6B7280))
        else -> Quad(Tint.blueBg, Color(0xFF3B82F6), "Agendado", Tint.blueFg)
    }
}

private data class Quad(val bg: Color, val border: Color, val label: String, val fg: Color)

/** Agenda (tela 22): Dia / Semana com faixa de dias, lista por horário, sheet com Confirmar/Conversar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(onConversationClick: (Int) -> Unit, onToast: (String) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("Dia") }
    var selected by remember { mutableStateOf(Calendar.getInstance()) }
    var weekOffset by remember { mutableIntStateOf(0) }
    var items by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var sheet by remember { mutableStateOf<Appointment?>(null) }

    val weekStart = remember(weekOffset) {
        Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }
    val days = remember(weekStart) { (0..6).map { (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, it) } } }

    LaunchedEffect(mode, selected.timeInMillis, weekStart.timeInMillis, reloadTick) {
        loading = true
        val fmt = SimpleDateFormat(ISO, Locale("pt", "BR"))
        val (start, end) = if (mode == "Dia") fmt.format(selected.time) to fmt.format(selected.time)
        else fmt.format(days.first().time) to fmt.format(days.last().time)
        AppContainer.mobileRepository.getAppointments(start, end).fold(
            onSuccess = { items = it.sortedBy { a -> a.startDate ?: "" }; error = null },
            onFailure = { error = it.message }
        )
        loading = false
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdAppHeader(title = "Agenda", presence = null) {
            Row(Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp))) {
                listOf("Dia", "Sem").forEach { m ->
                    val on = mode == m
                    Text(
                        m, fontSize = 11.sp, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (on) Color.White else c.text,
                        modifier = Modifier.background(if (on) c.primary else c.surface).clickable { mode = m }.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            VdHeaderIcon(Icons.Outlined.AddCircleOutline, "Novo agendamento", { onToast(WEB_ONLY) }, tint = c.primary)
        }

        // Faixa de dias
        Column(Modifier.fillMaxWidth().background(c.surface).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 18.sp, color = c.textTertiary, modifier = Modifier.clickable { weekOffset-- }.padding(horizontal = 8.dp))
                Text(
                    SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(days.first().time).replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text("›", fontSize = 18.sp, color = c.textTertiary, modifier = Modifier.clickable { weekOffset++ }.padding(horizontal = 8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day ->
                    val isSel = sameDay(day, selected)
                    val isWeekend = day.get(Calendar.DAY_OF_WEEK) in listOf(Calendar.SATURDAY, Calendar.SUNDAY)
                    val hasItems = items.any { a -> a.startDate?.let { sameDay(parse(it), day) } == true }
                    Column(
                        Modifier.width(44.dp).clickable { selected = day.clone() as Calendar; mode = "Dia" },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            SimpleDateFormat("EEE", Locale("pt", "BR")).format(day.time).replace(".", "").replaceFirstChar { it.uppercase() },
                            fontSize = 10.sp, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSel) c.primary else if (isWeekend) c.placeholder else c.muted
                        )
                        Box(
                            Modifier.size(30.dp).background(if (isSel) c.primary else Color.Transparent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${day.get(Calendar.DAY_OF_MONTH)}", fontSize = 13.sp, color = if (isSel) Color.White else if (isWeekend) c.placeholder else c.text)
                        }
                        Box(Modifier.size(4.dp).background(if (hasItems && !isSel) c.primary else Color.Transparent, CircleShape))
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

        when {
            loading && items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            error != null -> VdEmptyState(Icons.Outlined.CalendarMonth, "Não foi possível carregar", error ?: "")
            items.isEmpty() -> VdEmptyState(Icons.Outlined.CalendarMonth, "Nenhum agendamento", if (mode == "Dia") "Nada marcado para este dia." else "Nada marcado nesta semana.")
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp)) {
                if (mode == "Dia") {
                    items.forEach { a -> TimeSlotRow(a) { sheet = a } }
                } else {
                    val grouped = items.groupBy { a -> a.startDate?.let { parse(it) }?.let { d -> SimpleDateFormat("EEEE, d 'de' MMM", Locale("pt", "BR")).format(d.time) } ?: "Sem data" }
                    grouped.forEach { (label, list) ->
                        Text(label.replaceFirstChar { it.uppercase() }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.muted, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
                        list.forEach { a -> TimeSlotRow(a) { sheet = a } }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    sheet?.let { a ->
        val v = statusVisual(a.status)
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VdAvatar(name = a.contact ?: "?", size = 40.dp, fontSize = 12)
                    Column(Modifier.weight(1f)) {
                        Text("${a.contact ?: "Sem contato"} · ${a.service ?: a.title ?: "Agendamento"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(
                            listOfNotNull(a.startDate?.let { parse(it) }?.let { SimpleDateFormat("EEE d MMM · HH:mm", Locale("pt", "BR")).format(it.time) },
                                a.endDate?.let { parse(it) }?.let { SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(it.time) }, a.professional)
                                .joinToString(" – ").replace(" – ", " – ", true),
                            fontSize = 11.sp, color = c.muted
                        )
                    }
                    VdTag(v.label, color = v.fg, background = v.bg, pill = true)
                }
                if (!a.notes.isNullOrBlank()) {
                    Text(a.notes, fontSize = 12.sp, color = c.textTertiary, modifier = Modifier.background(c.surfaceSoft, RoundedCornerShape(8.dp)).border(1.dp, c.divider, RoundedCornerShape(8.dp)).padding(10.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VdButton("Reenviar", onClick = { onToast(WEB_ONLY) }, style = VdButtonStyle.Secondary, icon = Icons.Outlined.Send, height = 40.dp, modifier = Modifier.weight(1f))
                    VdButton("Conversar", onClick = {
                        val cid = a.contactId
                        if (cid == null) onToast("Agendamento sem contato") else scope.launch {
                            AppContainer.crmRepository.getContactConversationId(cid).onSuccess { id ->
                                if (id != null) { sheet = null; onConversationClick(id) } else onToast("Contato ainda não possui conversa")
                            }
                        }
                    }, style = VdButtonStyle.Secondary, icon = Icons.Outlined.Forum, height = 40.dp, modifier = Modifier.weight(1f))
                    if (a.status == "pending") {
                        VdButton("Confirmar", onClick = {
                            scope.launch {
                                AppContainer.mobileRepository.confirmAppointment(a.id).fold(
                                    onSuccess = { onToast("Agendamento confirmado"); sheet = null; reloadTick++ },
                                    onFailure = { onToast(it.message ?: "Erro ao confirmar") }
                                )
                            }
                        }, style = VdButtonStyle.Success, icon = Icons.Outlined.Check, height = 40.dp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSlotRow(a: Appointment, onClick: () -> Unit) {
    val c = AppTheme.colors
    val v = statusVisual(a.status)
    val start = a.startDate?.let { parse(it) }
    val end = a.endDate?.let { parse(it) }
    val canceled = a.status == "canceled" || a.status == "no_show"
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            start?.let { SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(it.time) } ?: "--:--",
            fontSize = 11.sp, color = c.muted, modifier = Modifier.width(36.dp).padding(top = 6.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
            Row(
                Modifier.fillMaxWidth().padding(top = 3.dp).clip(RoundedCornerShape(6.dp)).background(v.bg).clickable(onClick = onClick)
            ) {
                Box(Modifier.width(3.dp).height(46.dp).background(v.border))
                Column(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            a.contact ?: a.title ?: "Agendamento", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text,
                            textDecoration = if (canceled) TextDecoration.LineThrough else null, modifier = Modifier.weight(1f), maxLines = 1
                        )
                        Text(v.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = v.fg)
                    }
                    Text(
                        listOfNotNull(a.service ?: a.title, a.professional,
                            if (start != null && end != null) "${SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(start.time)}–${SimpleDateFormat("HH:mm", Locale("pt", "BR")).format(end.time)}" else null
                        ).joinToString(" · "),
                        fontSize = 11.sp, color = c.textTertiary, maxLines = 1
                    )
                }
            }
        }
    }
}

private fun parse(s: String): Calendar? =
    br.com.vipdesk.mobile.ui.common.parseApiDate(s)?.let { d -> Calendar.getInstance().apply { time = d } }

private fun sameDay(a: Calendar?, b: Calendar): Boolean =
    a != null && a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
