package br.com.vipdesk.mobile.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdKpiCard
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ————— Chat interno —————

/** Lista de conversas internas (GET internal-chat). */
@Composable
fun InternalChatListScreen(nav: ModuleNav) {
    ModuleListScreen(
        title = "Chat interno", subtitle = "Conversas com a equipe", onBack = nav.onBack,
        emptyTitle = "Nenhuma conversa", emptySubtitle = "Inicie um chat com a equipe pela versão web.",
        onRowClick = { row -> nav.onOpenChat(row.id.toInt(), row.title) }
    ) { _, _, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("internal-chat").map { body ->
            val (list, _) = body.rows("chats", "data")
            ModulePage(list.map { o ->
                val unread = o.int("unread_messages") ?: 0
                val participants = o.arr("participants")?.objects()?.mapNotNull { it.str("user_name") ?: it.str("name") }.orEmpty()
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: participants.joinToString(", ").ifBlank { "Chat" },
                    subtitle = o.str("last_message_text") ?: participants.joinToString(", "),
                    tag = if (unread > 0) "$unread não lidas" else null, tagBg = Tint.greenBg, tagFg = Tint.greenFg,
                    meta = fmtRelative(o.str("updated_at")), avatar = o.str("name") ?: participants.firstOrNull(), raw = o
                )
            }, null)
        }
    }
}

/** Sala do chat interno: mensagens (GET internal-chat/{id}) e envio (POST internal-chat/message). */
@Composable
fun InternalChatRoomScreen(chatId: Int, title: String, onBack: () -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    var myId by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()
    LaunchedEffect(toast) { if (toast != null) { delay(2200); toast = null } }

    suspend fun load() {
        apiGet("internal-chat/$chatId").fold(
            onSuccess = { body -> messages = body.rows("messages", "data").first; error = null },
            onFailure = { error = it.message }
        )
        loading = false
    }
    LaunchedEffect(chatId) {
        myId = AppContainer.tokenManager.getUserId()
        load()
        runCatching { AppContainer.apiService.postRaw("internal-chat/$chatId/mark-read") }
        // Sem evento de socket dedicado no app: atualiza a cada 8s enquanto a sala está aberta
        while (true) { delay(8000); load() }
    }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) runCatching { listState.scrollToItem(messages.size - 1) } }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            VdSubHeader(title = title, subtitle = "Chat interno", onBack = onBack)
            when {
                loading && messages.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                error != null && messages.isEmpty() -> Box(Modifier.weight(1f)) { VdEmptyState(Icons.Outlined.Forum, "Não foi possível carregar", error ?: "") }
                else -> LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(messages, key = { it.int("id") ?: it.hashCode() }) { m ->
                        val mine = m.int("from") == myId
                        val author = m.obj("user")?.str("name") ?: ""
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                            Column(
                                Modifier.widthIn(max = 300.dp)
                                    .background(if (mine) c.primarySurface else c.surface, RoundedCornerShape(12.dp))
                                    .border(1.dp, if (mine) c.primarySurface else c.divider, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (!mine && author.isNotBlank()) Text(author, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                                val body = m.str("text") ?: m.str("file_name")?.let { "📎 $it" } ?: ""
                                Text(body, fontSize = 13.sp, color = c.text, lineHeight = 18.sp)
                                Text(fmtDate(m.str("created_at"), "dd/MM HH:mm") ?: "", fontSize = 10.sp, color = c.muted, modifier = Modifier.align(Alignment.End).padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().background(c.surface).padding(horizontal = 12.dp, vertical = 8.dp).navigationBarsPadding().imePadding(),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.weight(1f).background(c.surfaceAlt, RoundedCornerShape(22.dp)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (text.isEmpty()) Text("Mensagem para a equipe…", fontSize = 13.sp, color = c.placeholder)
                    BasicTextField(value = text, onValueChange = { text = it }, textStyle = TextStyle(fontSize = 13.sp, color = c.text), cursorBrush = SolidColor(c.primary), modifier = Modifier.fillMaxWidth())
                }
                Box(
                    Modifier.size(40.dp).background(if (text.isNotBlank() && !sending) c.primary else c.placeholder, CircleShape).clickable(enabled = text.isNotBlank() && !sending) {
                        val body = text.trim(); text = ""; sending = true
                        scope.launch {
                            apiPost("internal-chat/message", json("chat_id" to chatId, "message" to body, "type" to "text")).fold({ load() }, { toast = it.message; text = body })
                            sending = false
                        }
                    }, contentAlignment = Alignment.Center
                ) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }
        toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)) { VdToast(it) } }
    }
}

// ————— Arena (gamificação) —————

@Composable
fun ArenaScreen(nav: ModuleNav) {
    val c = AppTheme.colors
    var me by remember { mutableStateOf<JsonObject?>(null) }
    var board by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf("Meu desempenho") }
    LaunchedEffect(Unit) {
        apiGet("gamification/me").fold({ me = it.takeIf { e -> e.isJsonObject }?.asJsonObject }, { error = it.message })
        apiGet("gamification/leaderboard").onSuccess { board = it.rows("rows", "data").first }
    }
    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(title = "Arena", subtitle = me?.obj("season")?.str("name") ?: "Gamificação da equipe", onBack = nav.onBack, below = {
            VdPillRow(contentPaddingStart = 0.dp) { listOf("Meu desempenho", "Ranking").forEach { t -> VdPill(t, tab == t, { tab = t }) } }
        })
        when {
            error != null -> VdEmptyState(Icons.Outlined.EmojiEvents, "Arena indisponível", error ?: "")
            me == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            me?.bool("enabled") == false -> VdEmptyState(Icons.Outlined.EmojiEvents, "Arena desativada", "A gamificação não está habilitada para esta empresa.")
            tab == "Ranking" -> LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(board, key = { it.int("user_id") ?: it.hashCode() }) { r ->
                    VdCard(leftBorder = if (r.bool("is_me") == true) c.primary else null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${r.int("position") ?: "-"}º", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if ((r.int("position") ?: 9) <= 3) Tint.amberFg else c.muted, modifier = Modifier.width(36.dp))
                            VdAvatar(name = r.str("display_name") ?: "?", size = 34.dp, fontSize = 12)
                            Column(Modifier.weight(1f)) {
                                Text(r.str("display_name") ?: "—", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                                Text("Nível ${r.int("level") ?: 0}", fontSize = 11.sp, color = c.muted)
                            }
                            Text("${r.int("total_xp") ?: 0} XP", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                        }
                    }
                }
            }
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val p = me?.obj("profile"); val m = me?.obj("metrics"); val s = me?.obj("sales")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${p?.int("total_xp") ?: 0}", "XP TOTAL", delta = "nível ${p?.int("level") ?: 0} · ${p?.int("progress_pct") ?: 0}% p/ próximo", modifier = Modifier.weight(1f))
                    VdKpiCard("${p?.int("streak_days") ?: 0}d", "SEQUÊNCIA", delta = "melhor: ${p?.int("best_streak_days") ?: 0} dias", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${m?.int("tickets_closed") ?: 0}", "TICKETS", delta = "fechados na temporada", modifier = Modifier.weight(1f))
                    VdKpiCard(m?.dbl("csat_avg")?.let { "%.1f".format(it) } ?: "—", "CSAT", delta = "${m?.int("csat_count") ?: 0} avaliações", modifier = Modifier.weight(1f))
                    VdKpiCard("${m?.int("sla_kept_count") ?: 0}/${(m?.int("sla_kept_count") ?: 0) + (m?.int("sla_breached_count") ?: 0)}", "SLA", delta = "dentro da meta", modifier = Modifier.weight(1f))
                }
                if (s?.bool("has_activity") == true) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${s.int("deals_won_count") ?: 0}", "GANHOS", delta = fmtMoney(s.dbl("deals_won_value")), modifier = Modifier.weight(1f))
                    VdKpiCard("${s.int("quotes_accepted_count") ?: 0}/${s.int("quotes_sent_count") ?: 0}", "ORÇAMENTOS", delta = "aceitos/enviados", modifier = Modifier.weight(1f))
                }
                val missions = me?.arr("missions")?.objects().orEmpty()
                if (missions.isNotEmpty()) {
                    VdSectionLabel("Missões")
                    missions.forEach { ms ->
                        VdCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(ms.str("title") ?: ms.str("name") ?: "Missão", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                                    ms.str("description")?.let { Text(it, fontSize = 11.sp, color = c.muted) }
                                }
                                val prog = ms.int("progress") ?: ms.int("current"); val target = ms.int("target") ?: ms.int("goal")
                                if (prog != null && target != null) VdTag("$prog/$target", color = Tint.indigoFg, background = Tint.indigoBg, pill = true)
                                else if (ms.bool("completed") == true) VdTag("Concluída", color = Tint.greenFg, background = Tint.greenBg, pill = true)
                            }
                        }
                    }
                }
                val ach = me?.arr("achievements")?.objects().orEmpty()
                if (ach.isNotEmpty()) {
                    VdSectionLabel("Conquistas")
                    ach.forEach { a -> VdCard { Text(a.str("title") ?: a.str("name") ?: "Conquista", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text); a.str("description")?.let { Text(it, fontSize = 11.sp, color = c.muted) } } }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ————— Dashboards de vendas —————

@Composable
fun SalesDashboardScreen(nav: ModuleNav) {
    val c = AppTheme.colors
    var scope by remember { mutableStateOf("Meus números") }
    var overview by remember { mutableStateOf<JsonObject?>(null) }
    var funnel by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(scope) {
        overview = null; funnel = null
        val mine = scope == "Meus números"
        apiGet(if (mine) "sales-report/my-overview" else "sales-report/overview").fold({ overview = it.asJsonObject }, { error = it.message })
        apiGet(if (mine) "sales-report/my-funnel" else "sales-report/funnel").onSuccess { funnel = it.takeIf { e -> e.isJsonObject }?.asJsonObject }
    }
    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(title = "Dashboards de vendas", subtitle = overview?.obj("period")?.let { "${fmtDate(it.str("from"), "dd/MM")} – ${fmtDate(it.str("to"), "dd/MM")}" } ?: "Mês atual", onBack = nav.onBack, below = {
            VdPillRow(contentPaddingStart = 0.dp) { listOf("Meus números", "Equipe").forEach { t -> VdPill(t, scope == t, { scope = t }) } }
        })
        when {
            error != null -> VdEmptyState(Icons.Outlined.EmojiEvents, "Relatório indisponível", error ?: "")
            overview == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val o = overview!!
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard(fmtMoney(o.dbl("won_value")), "GANHOS", delta = "${o.int("won_deals") ?: 0} negócios", modifier = Modifier.weight(1f), topAccent = Tint.greenFg)
                    VdKpiCard(fmtMoney(o.dbl("open_value")), "EM ABERTO", delta = "${o.int("open_deals") ?: 0} negócios", modifier = Modifier.weight(1f), topAccent = Tint.blueFg)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${o.dbl("win_rate")?.let { "%.1f".format(it) } ?: "0"}%", "TAXA DE GANHO", delta = "${o.int("lost_deals") ?: 0} perdidos", modifier = Modifier.weight(1f))
                    VdKpiCard(fmtMoney(o.dbl("avg_ticket")), "TICKET MÉDIO", delta = "${o.int("total_deals") ?: 0} no total", modifier = Modifier.weight(1f))
                }
                funnel?.let { f ->
                    VdSectionLabel("Funil · ${f.str("pipeline_name") ?: ""}")
                    VdCard {
                        val stages = f.arr("stages")?.objects().orEmpty()
                        val max = stages.maxOfOrNull { it.int("deals") ?: 0 }?.coerceAtLeast(1) ?: 1
                        stages.forEach { st ->
                            val n = st.int("deals") ?: 0
                            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(st.str("stage_name") ?: "", fontSize = 12.sp, color = c.text, modifier = Modifier.width(120.dp), maxLines = 1)
                                Box(Modifier.weight(1f).height(10.dp).background(c.surfaceAlt, RoundedCornerShape(5.dp))) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(n.toFloat() / max).background(st.str("stage_color")?.let { runCatching { Color(0xFF000000 or it.removePrefix("#").toLong(16)) }.getOrNull() } ?: c.primary, RoundedCornerShape(5.dp)))
                                }
                                Text("$n · ${fmtMoney(st.dbl("value"))}", fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        if (stages.isEmpty()) Text("Sem negócios no período.", fontSize = 12.sp, color = c.muted)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ————— SDR · agendamentos, performance —————

@Composable
fun SdrScreen(nav: ModuleNav) {
    val c = AppTheme.colors
    var dash by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { apiGet("appointments/dashboard").fold({ dash = it.asJsonObject }, { error = it.message }) }
    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(title = "SDR · Agendamentos", subtitle = dash?.str("date")?.let { "Hoje · ${fmtDate(it, "dd/MM")}" } ?: "Performance de agendamentos", onBack = nav.onBack)
        when {
            error != null -> VdEmptyState(Icons.Outlined.EmojiEvents, "Indisponível", error ?: "")
            dash == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val t = dash!!.obj("totals")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${t?.int("today") ?: 0}", "HOJE", delta = "agendamentos", modifier = Modifier.weight(1f))
                    VdKpiCard("${t?.int("pending") ?: 0}", "PENDENTES", delta = "a confirmar", modifier = Modifier.weight(1f), topAccent = Tint.amberFg)
                    VdKpiCard("${t?.int("confirmed") ?: 0}", "CONFIRMADOS", delta = "hoje", modifier = Modifier.weight(1f), topAccent = Tint.greenFg)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VdKpiCard("${t?.int("completed") ?: 0}", "CONCLUÍDOS", delta = "atendidos", modifier = Modifier.weight(1f))
                    VdKpiCard("${t?.int("canceled") ?: 0}", "CANCELADOS", delta = "hoje", modifier = Modifier.weight(1f))
                    VdKpiCard("${t?.int("no_show") ?: 0}", "NO-SHOW", delta = "não compareceram", modifier = Modifier.weight(1f), topAccent = Tint.redFg)
                }
                val trend = dash!!.arr("trend")?.objects().orEmpty()
                if (trend.isNotEmpty()) {
                    VdSectionLabel("Últimos 14 dias")
                    VdCard {
                        val max = trend.maxOfOrNull { it.int("qty") ?: 0 }?.coerceAtLeast(1) ?: 1
                        Row(Modifier.fillMaxWidth().height(90.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                            trend.forEach { d ->
                                val q = d.int("qty") ?: 0
                                Box(Modifier.weight(1f).fillMaxHeight(q.toFloat() / max).background(c.primary, RoundedCornerShape(3.dp)))
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(fmtDate(trend.first().str("date"), "dd/MM") ?: "", fontSize = 10.sp, color = c.muted)
                            Text(fmtDate(trend.last().str("date"), "dd/MM") ?: "", fontSize = 10.sp, color = c.muted)
                        }
                    }
                }
                val upcoming = dash!!.arr("upcoming")?.objects().orEmpty()
                if (upcoming.isNotEmpty()) {
                    VdSectionLabel("Próximos")
                    upcoming.forEach { a -> VdCard { Text(a.str("contact") ?: a.str("phone_number") ?: "Agendamento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text); Text(listOfNotNull(fmtDate(a.str("start_date")), a.str("service")).joinToString(" · "), fontSize = 11.sp, color = c.muted) } }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
