package br.com.vipdesk.mobile.ui.modules

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.ui.platform.LocalContext
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonObject

/** Chaves dos módulos acessíveis pela gaveta "Mais" e pelos hubs. */
object ModuleKeys {
    const val CAMPAIGNS = "campaigns"
    const val DESKFLOW = "deskflow"
    const val SURVEYS = "surveys"
    const val SENTIMENT = "sentiment"
    const val MEETINGS = "meetings"
    const val NOTES = "notes"
    const val CALLS = "calls"
    const val KB = "kb"
    const val APPROVALS = "approvals"
    const val ASSETS = "assets"
    const val HOURS = "hours"
    const val PROJECTS = "projects"
    const val SLA = "sla"
    const val CADENCES = "cadences"
    const val DIALER = "dialer"
    const val QUOTES = "quotes"
    const val LANDING = "landing"
    const val PIPELINES = "pipelines"
    const val BILLING = "billing"
    const val CONFIRMATIONS = "confirmations"
    const val ADMIN = "admin"
    const val OBSERVABILITY = "observability"
    const val PLAN = "plan"
    const val BASE_LEGAL = "baselegal"
    const val CLIENTS = "clients"
    const val GOALS = "goals"
    const val INTERNAL_CHAT = "internalchat"
    const val ARENA = "arena"
    const val SALES = "sales"
    const val SDR = "sdr"
    const val QUICK_REPLIES = "quickreplies"
}

data class ModuleNav(
    val onBack: () -> Unit,
    val onOpenTicket: (Int) -> Unit = {},
    val onOpenConversation: (Int) -> Unit = {},
    val onOpenContact: (Int) -> Unit = {},
    val onOpenDeal: (Int) -> Unit = {},
    val onOpenChat: (Int, String) -> Unit = { _, _ -> }
)

/** Despacha a chave do módulo para a tela correspondente. */
@Composable
fun ModuleScreen(key: String, nav: ModuleNav) {
    androidx.compose.runtime.CompositionLocalProvider(LocalModuleNav provides nav) { ModuleContent(key, nav) }
}

@Composable
private fun ModuleContent(key: String, nav: ModuleNav) {
    when (key) {
        ModuleKeys.CAMPAIGNS -> CampaignsModule(nav)
        ModuleKeys.DESKFLOW -> DeskFlowModule(nav)
        ModuleKeys.SURVEYS -> SurveysModule(nav)
        ModuleKeys.SENTIMENT -> SentimentModule(nav)
        ModuleKeys.MEETINGS -> MeetingsModule(nav)
        ModuleKeys.NOTES -> NotesModule(nav)
        ModuleKeys.CALLS -> CallsModule(nav)
        ModuleKeys.KB -> KnowledgeBaseModule(nav)
        ModuleKeys.APPROVALS -> ApprovalsModule(nav)
        ModuleKeys.ASSETS -> AssetsModule(nav)
        ModuleKeys.HOURS -> HoursApprovalModule(nav)
        ModuleKeys.PROJECTS -> ProjectsModule(nav)
        ModuleKeys.SLA -> SlaModule(nav)
        ModuleKeys.CADENCES -> CadencesModule(nav)
        ModuleKeys.DIALER -> DialerModule(nav)
        ModuleKeys.QUOTES -> QuotesModule(nav)
        ModuleKeys.LANDING -> LandingPagesModule(nav)
        ModuleKeys.PIPELINES -> PipelinesConfigModule(nav)
        ModuleKeys.BILLING -> BillingModule(nav)
        ModuleKeys.CONFIRMATIONS -> ConfirmationsModule(nav)
        ModuleKeys.ADMIN -> AdminModule(nav)
        ModuleKeys.OBSERVABILITY -> ObservabilityModule(nav)
        ModuleKeys.PLAN -> PlanModule(nav)
        ModuleKeys.BASE_LEGAL -> BaseLegalModule(nav)
        ModuleKeys.CLIENTS -> ClientsModule(nav)
        ModuleKeys.GOALS -> GoalsModule(nav)
        ModuleKeys.INTERNAL_CHAT -> InternalChatListScreen(nav)
        ModuleKeys.ARENA -> ArenaScreen(nav)
        ModuleKeys.SALES -> SalesDashboardScreen(nav)
        ModuleKeys.SDR -> SdrScreen(nav)
        ModuleKeys.QUICK_REPLIES -> QuickRepliesModule(nav)
        else -> ModuleListScreen(title = key, onBack = nav.onBack) { _, _, _, _ -> Result.success(ModulePage(emptyList(), 0)) }
    }
}

private fun pageParams(query: String, skip: Int, take: Int, searchKey: String = "search") =
    buildMap { put("take", take.toString()); put("skip", skip.toString()); if (query.isNotBlank()) put(searchKey, query) }

private fun openUrl(context: android.content.Context, url: String?) {
    if (url.isNullOrBlank()) return
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

// ————— Campanhas (tela 25) —————

@Composable
private fun CampaignsModule(nav: ModuleNav) {
    ModuleListScreen(
        title = "Campanhas", onBack = nav.onBack, searchHint = "Buscar campanha",
        filters = listOf("Todas", "Em andamento", "Aguardando", "Em pausa", "Finalizadas"),
        actions = { row ->
            val paused = row.raw?.bool("paused") == true
            val finished = row.raw?.int("status") == 2
            buildList {
                if (!finished) add(ModuleAction(if (paused) "Retomar campanha" else "Pausar campanha") { r ->
                    apiPost("campaing/pause", json("data" to r.id, "paused" to !paused)).map { if (paused) "Campanha retomada" else "Campanha pausada" }
                })
            }
        }
    ) { q, f, skip, take ->
        apiGet("campaing", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            val rows = list.map { o ->
                val paused = o.bool("paused") == true
                val st = when { paused -> "paused"; o.int("status") == -1 -> "interrupted"; o.int("status") == 2 -> "finished"; o.int("status") == 1 -> "running"; else -> "waiting" }
                val (label, bg, fg) = when (st) {
                    "paused" -> Triple("Em pausa", Tint.yellowBg, Tint.yellowFg)
                    "running" -> Triple("Em andamento", Tint.blueBg, Tint.blueFg)
                    "finished" -> Triple("Finalizada", Tint.greenBg, Tint.greenFg)
                    "interrupted" -> Triple("Interrompida", Tint.redBg, Tint.redFg)
                    else -> Triple("Aguardando início", Tint.yellowBg, Tint.amberFg)
                }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("title") ?: "Campanha ${o.int("id")}",
                    subtitle = o.str("message")?.take(120), tag = label, tagBg = bg, tagFg = fg,
                    meta = fmtDate(o.str("start_date"), "dd/MM HH:mm"),
                    details = listOfNotNull(
                        "Início" to (fmtDate(o.str("start_date")) ?: "—"),
                        "Conta" to (o.str("account_number") ?: "—"),
                        "Departamento" to (o.str("department") ?: "—"),
                        "Enviadas" to (o.str("total_messages") ?: o.str("sent_messages") ?: "—"),
                        "Entregues" to (o.str("delivered_messages") ?: "—"),
                        "Lidas" to (o.str("read_messages") ?: "—"),
                        "Respostas" to (o.str("total_answered") ?: "—"),
                        "Responsável" to (o.str("user") ?: o.obj("user")?.str("name") ?: "—")
                    ),
                    body = o.str("message"), raw = o
                )
            }
            val filtered = when (f) {
                "Em andamento" -> rows.filter { it.tag == "Em andamento" }
                "Aguardando" -> rows.filter { it.tag == "Aguardando início" }
                "Em pausa" -> rows.filter { it.tag == "Em pausa" }
                "Finalizadas" -> rows.filter { it.tag == "Finalizada" }
                else -> rows
            }
            ModulePage(filtered, if (f == "Todas") total else null)
        }
    }
}

// ————— DeskFlow (tela 28, somente leitura) —————

@Composable
private fun DeskFlowModule(nav: ModuleNav) {
    ModuleListScreen(title = "DeskFlow", subtitle = "Fluxos de automação · edição no desktop", onBack = nav.onBack, searchHint = "Buscar fluxo") { q, _, skip, take ->
        apiGet("bot-flow", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val nodes = runCatching {
                    val data = o.get("data")?.let { d -> if (d.isJsonPrimitive) com.google.gson.JsonParser.parseString(d.asString) else d }
                    data?.asJsonObject?.arr("form")?.objects()?.mapNotNull { n -> n.str("text") ?: n.str("name") ?: n.str("title") }.orEmpty()
                }.getOrDefault(emptyList())
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: "Fluxo ${o.int("id")}",
                    subtitle = if (nodes.isEmpty()) "Sem etapas" else "${nodes.size} etapas · ${nodes.take(3).joinToString(" › ")}",
                    meta = fmtRelative(o.str("updated_at")),
                    details = nodes.mapIndexed { i, n -> "Etapa ${i + 1}" to n },
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Pesquisas de satisfação (tela 30) —————

@Composable
private fun SurveysModule(nav: ModuleNav) {
    ModuleListScreen(
        title = "Pesquisas", subtitle = "Respostas de satisfação", onBack = nav.onBack, searchHint = "Contato, atendente ou protocolo",
        actions = { row -> row.raw?.obj("ticket")?.int("id")?.let { tid -> listOf(ModuleAction("Abrir ticket") { Result.success("open-ticket:$tid") }) } ?: emptyList() }
    ) { q, _, skip, take ->
        apiGet("user-survey", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                // `rating` é a nota (1–5); vazio = pesquisa enviada e ainda sem resposta
                val score = o.str("rating")?.toIntOrNull()
                val (bg, fg) = when { score == null -> Tint.grayBg to Tint.grayFg; score >= 4 -> Tint.greenBg to Tint.greenFg; score == 3 -> Tint.yellowBg to Tint.yellowFg; else -> Tint.redBg to Tint.redFg }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("contact") ?: o.str("phone_number") ?: "Contato",
                    subtitle = listOfNotNull(o.str("user")?.let { "Atendente: $it" }, o.str("ticket_number")?.let { "#$it" }).joinToString(" · "),
                    tag = score?.let { "Nota $it/5" } ?: "Sem resposta", tagBg = bg, tagFg = fg,
                    meta = o.str("created_at")?.take(10), avatar = o.str("contact"),
                    body = o.str("comment"),
                    details = listOfNotNull(("Insatisfação" to (o.str("dissatisfaction") ?: "—")).takeIf { o.str("dissatisfaction") != null }),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Análise de sentimento / mensagens classificadas —————

@Composable
private fun SentimentModule(nav: ModuleNav) {
    ModuleListScreen(
        title = "Análise de sentimento", subtitle = "Classificação por IA (XLA)", onBack = nav.onBack,
        filters = listOf("Todos", "Positivo", "Neutro", "Negativo"),
        actions = { row -> row.raw?.int("ticket_id")?.let { tid -> listOf(ModuleAction("Abrir ticket") { Result.success("open-ticket:$tid") }) } ?: emptyList() }
    ) { _, f, skip, take ->
        val page = skip / take + 1
        val params = mutableMapOf("per_page" to take.toString(), "page" to page.toString())
        when (f) { "Positivo" -> params["sentiment"] = "positive"; "Neutro" -> params["sentiment"] = "neutral"; "Negativo" -> params["sentiment"] = "negative" }
        apiGet("sentiment-analysis", params).map { body ->
            val (list, total) = body.rows()
            val rows = list.map { o ->
                val s = o.str("customer_sentiment")
                val (label, bg, fg) = when (s) { "positive" -> Triple("Positivo", Tint.greenBg, Tint.greenFg); "negative" -> Triple("Negativo", Tint.redBg, Tint.redFg); "neutral" -> Triple("Neutro", Tint.grayBg, Tint.grayFg); else -> Triple(s ?: "—", Tint.grayBg, Tint.grayFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("topic") ?: o.str("summary")?.take(60) ?: "Análise ${o.int("id")}",
                    subtitle = o.str("summary")?.take(140), tag = label, tagBg = bg, tagFg = fg,
                    meta = o.int("customer_score")?.let { "cliente $it/10" },
                    body = o.str("summary"),
                    details = listOfNotNull(
                        "Ticket" to (o.int("ticket_id")?.toString() ?: "—"),
                        "Nota do cliente" to (o.int("customer_score")?.toString() ?: "—"),
                        "Nota do agente" to (o.int("agent_score")?.toString() ?: "—"),
                        "Risco de churn" to (o.str("risk_of_churn") ?: "—"),
                        "Desfecho" to (o.str("conversation_outcome") ?: "—"),
                        "Resolvido" to (if (o.bool("resolved") == true) "Sim" else "Não")
                    ),
                    raw = o
                )
            }
            val filtered = when (f) { "Positivo" -> rows.filter { it.tag == "Positivo" }; "Neutro" -> rows.filter { it.tag == "Neutro" }; "Negativo" -> rows.filter { it.tag == "Negativo" }; else -> rows }
            ModulePage(filtered, total)
        }
    }
}

// ————— Reuniões —————

@Composable
private fun MeetingsModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Reuniões", subtitle = "Salas de videochamada", onBack = nav.onBack,
        actions = { row -> listOfNotNull(row.raw?.str("join_url")?.let { url -> ModuleAction("Entrar na reunião", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo reunião no navegador") } }) }
    ) { _, _, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("meet/rooms").map { body ->
            val (list, _) = body.rows("rooms", "data")
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "OPEN", "LIVE", "ACTIVE" -> Triple("Ao vivo", Tint.greenBg, Tint.greenFg); "CLOSED" -> Triple("Encerrada", Tint.grayBg, Tint.grayFg); "SCHEDULED" -> Triple("Agendada", Tint.blueBg, Tint.blueFg); else -> Triple(st ?: "—", Tint.grayBg, Tint.grayFg) }
                ModuleRow(
                    id = o.str("id") ?: o.str("slug") ?: "", title = o.str("title") ?: "Reunião",
                    subtitle = listOfNotNull(o.obj("created_by")?.str("name")?.let { "por $it" }, o.int("participantsTotal")?.let { "$it participantes" }).joinToString(" · "),
                    tag = label, tagBg = bg, tagFg = fg, meta = fmtRelative(o.str("startedAt") ?: o.str("createdAt")),
                    details = listOfNotNull("Início" to (fmtDate(o.str("startedAt")) ?: "—"), "Fim" to (fmtDate(o.str("endedAt")) ?: "—"), "Link" to (o.str("join_url") ?: "—")),
                    raw = o
                )
            }, null)
        }
    }
}

// ————— Post-its —————

@Composable
private fun NotesModule(nav: ModuleNav) {
    var showNew by remember { mutableStateOf(false) }
    var version by remember { mutableStateOf(0) }
    if (showNew) TextPromptSheet(title = "Novo post-it", hint = "Escreva sua nota", onDismiss = { showNew = false }) { text ->
        apiPost("notes", json("content" to text, "pin_color" to "yellow")).map { version++; "Post-it criado" }
    }
    ModuleListScreen(
        title = "Post-its", subtitle = "Suas anotações rápidas", onBack = nav.onBack, reloadKey = version,
        emptyTitle = "Nenhum post-it", emptySubtitle = "Toque em + para criar uma nota.",
        headerActions = { br.com.vipdesk.mobile.ui.components.VdHeaderIcon(Icons.Outlined.AddCircleOutline, "Novo post-it", { showNew = true }) },
        actions = { row -> listOf(ModuleAction("Excluir post-it", VdButtonStyle.Destructive) { r -> apiDelete("notes/${r.id}").map { "Post-it excluído" } }) }
    ) { _, _, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("notes").map { body ->
            val (list, _) = body.rows()
            ModulePage(list.map { o ->
                ModuleRow(id = o.int("id").toString(), title = o.str("content")?.lines()?.firstOrNull()?.take(60) ?: "(vazio)", subtitle = o.str("content")?.take(160), meta = fmtRelative(o.str("updated_at") ?: o.str("created_at")), body = o.str("content"), leftBorder = Tint.yellowFg, raw = o)
            }, null)
        }
    }
}

// ————— Ligações (call-records) —————

@Composable
private fun CallsModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Ligações", subtitle = "Histórico da telefonia", onBack = nav.onBack, searchHint = "Número, contato ou atendente",
        filters = listOf("Todas", "Recebidas", "Realizadas"),
        actions = { row -> buildList {
            row.raw?.str("recording_public_url")?.let { url -> add(ModuleAction("Ouvir gravação", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo gravação") }) }
            row.raw?.int("ticket_id")?.let { tid -> add(ModuleAction("Abrir ticket") { Result.success("open-ticket:$tid") }) }
            row.raw?.int("conversation_id")?.let { cid -> add(ModuleAction("Abrir conversa") { Result.success("open-conversation:$cid") }) }
        } }
    ) { q, f, skip, take ->
        val params = pageParams(q, skip, take).toMutableMap()
        when (f) { "Recebidas" -> params["direction"] = "in"; "Realizadas" -> params["direction"] = "out" }
        apiGet("call-records", params).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val dir = o.str("direction"); val res = o.str("result")
                val (bg, fg) = when (res) { "atendida", "answered" -> Tint.greenBg to Tint.greenFg; "perdida", "missed", "no_answer" -> Tint.redBg to Tint.redFg; else -> Tint.grayBg to Tint.grayFg }
                val dur = o.int("duration")?.let { "${it / 60}m${(it % 60).toString().padStart(2, '0')}s" }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("contact_name") ?: (if (dir == "in") o.str("caller") else o.str("destination")) ?: "Ligação",
                    subtitle = listOfNotNull(if (dir == "in") "Recebida" else "Realizada", o.str("user_name")?.let { "atendente $it" }, dur).joinToString(" · "),
                    tag = res?.replaceFirstChar { it.uppercase() }, tagBg = bg, tagFg = fg, meta = o.str("created_at")?.take(16), avatar = o.str("contact_name") ?: (if (dir == "in") o.str("caller") else o.str("destination")),
                    details = listOfNotNull("De" to (o.str("caller") ?: "—"), "Para" to (o.str("destination") ?: "—"), "Duração" to (dur ?: "—"), "Protocolo" to (o.str("protocol") ?: "—"), "Ticket" to (o.str("ticket_number") ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Base de Conhecimento —————

@Composable
private fun KnowledgeBaseModule(nav: ModuleNav) {
    ModuleListScreen(title = "Base de Conhecimento", subtitle = "Artigos internos", onBack = nav.onBack, searchHint = "Buscar artigo") { q, _, skip, take ->
        apiGet("kb/article", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val content = (o.str("content") ?: o.str("body") ?: o.str("summary") ?: "").replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("title") ?: "Artigo ${o.int("id")}",
                    subtitle = listOfNotNull(o.obj("category")?.str("name"), o.int("views")?.let { "$it visualizações" }).joinToString(" · ").ifBlank { content.take(100) },
                    tag = o.str("status")?.let { if (it == "published") "Publicado" else it }, meta = fmtRelative(o.str("updated_at")),
                    body = content.take(4000).ifBlank { null }, raw = o
                )
            }, total)
        }
    }
}

// ————— Aprovações de ticket —————

@Composable
private fun ApprovalsModule(nav: ModuleNav) {
    var reasonFor by remember { mutableStateOf<Pair<ModuleRow, String>?>(null) }
    var version by remember { mutableStateOf(0) }
    reasonFor?.let { (row, decision) ->
        TextPromptSheet(title = if (decision == "approve") "Aprovar solicitação" else "Rejeitar solicitação", hint = "Justificativa (opcional)", allowEmpty = true, onDismiss = { reasonFor = null }) { text ->
            apiPost("ticket-approval/${row.id}/$decision", json("decision_reason" to text.ifBlank { null })).map { version++; if (decision == "approve") "Aprovado" else "Rejeitado" }
        }
    }
    ModuleListScreen(
        title = "Aprovações", subtitle = "Solicitações em tickets", onBack = nav.onBack, reloadKey = version,
        filters = listOf("Pendentes", "Aprovadas", "Rejeitadas", "Todas"),
        actions = { row -> buildList {
            if (row.raw?.str("status") == "pending") {
                add(ModuleAction("Aprovar", VdButtonStyle.Primary) { r -> reasonFor = r to "approve"; Result.success("") })
                add(ModuleAction("Rejeitar", VdButtonStyle.Destructive) { r -> reasonFor = r to "reject"; Result.success("") })
            }
            row.raw?.int("ticket_id")?.let { tid -> add(ModuleAction("Abrir ticket") { Result.success("open-ticket:$tid") }) }
        } }
    ) { _, f, skip, take ->
        // scope=queue (padrão do backend) só devolve pendentes; os demais filtros precisam de scope=all
        val params = mutableMapOf("take" to take.toString(), "skip" to skip.toString())
        when (f) {
            "Pendentes" -> params["status"] = "pending"
            "Aprovadas" -> { params["scope"] = "all"; params["status"] = "approved" }
            "Rejeitadas" -> { params["scope"] = "all"; params["status"] = "rejected" }
            else -> params["scope"] = "all"
        }
        apiGet("ticket-approval", params).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "approved" -> Triple("Aprovada", Tint.greenBg, Tint.greenFg); "rejected" -> Triple("Rejeitada", Tint.redBg, Tint.redFg); "canceled", "cancelled" -> Triple("Cancelada", Tint.grayBg, Tint.grayFg); else -> Triple("Pendente", Tint.yellowBg, Tint.yellowFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("subject") ?: "Aprovação ${o.int("id")}",
                    subtitle = listOfNotNull(o.obj("requester")?.str("name")?.let { "por $it" }, o.obj("ticket")?.str("ticket_number")?.let { "#$it" }).joinToString(" · "),
                    tag = label, tagBg = bg, tagFg = fg, meta = fmtRelative(o.str("created_at")),
                    body = o.str("reason"),
                    details = listOfNotNull("Bloqueia o ticket" to (if (o.bool("blocks_progress") == true) "Sim" else "Não"), "Decidido por" to (o.obj("decider")?.str("name") ?: "—"), "Justificativa" to (o.str("decision_reason") ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Inventário (ativos) —————

@Composable
private fun AssetsModule(nav: ModuleNav) {
    ModuleListScreen(title = "Inventário", subtitle = "Ativos · licenças · CMDB", onBack = nav.onBack, searchHint = "Nome, tag, série ou modelo") { q, _, skip, take ->
        apiGet("asset", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "in_use" -> Triple("Em uso", Tint.greenBg, Tint.greenFg); "in_stock" -> Triple("Em estoque", Tint.blueBg, Tint.blueFg); "maintenance" -> Triple("Manutenção", Tint.yellowBg, Tint.yellowFg); "retired", "disposed" -> Triple("Baixado", Tint.redBg, Tint.redFg); else -> Triple(st ?: "—", Tint.grayBg, Tint.grayFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: "Ativo ${o.int("id")}",
                    subtitle = listOfNotNull(o.str("manufacturer"), o.str("model"), o.str("asset_tag")?.let { "tag $it" }).joinToString(" · "),
                    tag = label, tagBg = bg, tagFg = fg, meta = o.str("type"),
                    details = listOfNotNull("Série" to (o.str("serial_number") ?: "—"), "Categoria" to (o.obj("category")?.str("name") ?: "—"), "Local" to (o.obj("location")?.str("name") ?: "—"), "Responsável" to (o.obj("owner_user")?.str("name") ?: o.obj("owner_contact")?.str("name") ?: "—"), "Garantia até" to (fmtDate(o.str("warranty_expires_at"), "dd/MM/yyyy") ?: "—"), "Observações" to (o.str("notes") ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Aprovar horas (worklogs) —————

@Composable
private fun HoursApprovalModule(nav: ModuleNav) {
    var reasonFor by remember { mutableStateOf<Pair<ModuleRow, String>?>(null) }
    var version by remember { mutableStateOf(0) }
    reasonFor?.let { (row, decision) ->
        TextPromptSheet(title = if (decision == "approve") "Aprovar horas" else "Rejeitar horas", hint = "Motivo (opcional)", allowEmpty = true, onDismiss = { reasonFor = null }) { text ->
            apiPost("time-entry/${row.id}/$decision", json("reason" to text.ifBlank { null })).map { version++; if (decision == "approve") "Horas aprovadas" else "Horas rejeitadas" }
        }
    }
    ModuleListScreen(
        title = "Aprovar horas", subtitle = "Worklogs pendentes da equipe", onBack = nav.onBack, reloadKey = version,
        emptyTitle = "Nada pendente", emptySubtitle = "Nenhum lançamento aguardando aprovação.",
        actions = { row -> buildList {
            add(ModuleAction("Aprovar", VdButtonStyle.Primary) { r -> reasonFor = r to "approve"; Result.success("") })
            add(ModuleAction("Rejeitar", VdButtonStyle.Destructive) { r -> reasonFor = r to "reject"; Result.success("") })
            row.raw?.int("ticket_id")?.let { tid -> add(ModuleAction("Abrir ticket") { Result.success("open-ticket:$tid") }) }
        } }
    ) { _, _, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("time-entry/pending").map { body ->
            val (list, _) = body.rows()
            ModulePage(list.map { o ->
                val mins = o.int("duration_minutes") ?: 0
                ModuleRow(
                    id = o.int("id").toString(), title = o.obj("user")?.str("name") ?: o.str("user_name") ?: "Lançamento ${o.int("id")}",
                    subtitle = listOfNotNull(o.obj("ticket")?.str("ticket_number")?.let { "#$it" }, o.obj("ticket")?.str("title"), o.str("activity_type")).joinToString(" · "),
                    tag = "${mins / 60}h${(mins % 60).toString().padStart(2, '0')}", tagBg = Tint.blueBg, tagFg = Tint.blueFg,
                    meta = fmtDate(o.str("started_at"), "dd/MM HH:mm"), body = o.str("description") ?: o.str("notes"),
                    details = listOfNotNull("Faturável" to (if (o.bool("billable") == true) "Sim" else "Não"), "Departamento" to (o.obj("department")?.str("name") ?: "—")),
                    raw = o
                )
            }, null)
        }
    }
}

// ————— Projetos —————

@Composable
private fun ProjectsModule(nav: ModuleNav) {
    ModuleListScreen(title = "Projetos", subtitle = "Quadros agrupados por projeto", onBack = nav.onBack) { _, _, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("kanban/projects").map { body ->
            val (list, _) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "active", "in_progress" -> Triple("Em andamento", Tint.blueBg, Tint.blueFg); "completed", "done" -> Triple("Concluído", Tint.greenBg, Tint.greenFg); "planned" -> Triple("Planejado", Tint.yellowBg, Tint.yellowFg); "on_hold" -> Triple("Em espera", Tint.grayBg, Tint.grayFg); else -> Triple(st ?: "—", Tint.grayBg, Tint.grayFg) }
                val open = o.int("tasks_open_count") ?: 0; val done = o.int("tasks_completed_count") ?: 0; val total = o.int("tasks_total_count") ?: (open + done)
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: "Projeto",
                    subtitle = "$done de $total tarefas concluídas · ${o.int("boards_count") ?: 0} quadros" + (o.int("tasks_overdue_count")?.takeIf { it > 0 }?.let { " · $it atrasadas" } ?: ""),
                    tag = label, tagBg = bg, tagFg = fg, meta = o.obj("owner")?.str("name"),
                    leftBorder = o.str("color")?.let { runCatching { androidx.compose.ui.graphics.Color(0xFF000000 or it.removePrefix("#").toLong(16)) }.getOrNull() },
                    details = listOfNotNull("Início" to (o.str("start_date") ?: "—"), "Fim" to (o.str("end_date") ?: "—"), "Responsável" to (o.obj("owner")?.str("name") ?: "—"), "Ciclo médio (30d)" to ((o.dbl("avg_cycle_days_30d")?.let { "$it dias" }) ?: "—"), "Descrição" to (o.str("description") ?: "—")),
                    raw = o
                )
            }, null)
        }
    }
}

// ————— SLA por prioridade · Categorias —————

@Composable
private fun SlaModule(nav: ModuleNav) {
    ModuleListScreen(title = "SLA", subtitle = "Políticas por prioridade e categoria", onBack = nav.onBack, filters = listOf("SLA", "Categorias")) { _, f, skip, take ->
        if (f == "Categorias") apiGet("ticket-category", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o -> ModuleRow(id = "cat-${o.int("id")}", title = o.str("description") ?: o.str("name") ?: "Categoria", subtitle = o.obj("parent")?.str("description")?.let { "em $it" }, raw = o) }, total)
        } else apiGet("sla", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val pr = o.str("priority")
                val prLabel = when (pr) { "very_high", "urgent" -> "Urgente"; "high" -> "Alta"; "medium", "normal" -> "Média"; "low" -> "Baixa"; else -> pr ?: "—" }
                ModuleRow(
                    id = "sla-${o.int("id")}", title = o.str("name") ?: "SLA",
                    subtitle = "1ª resposta ${o.int("first_response_minutes") ?: 0} min · resolução ${o.int("resolution_minutes") ?: 0} min",
                    tag = if (o.bool("active") == false) "Inativo" else prLabel, tagBg = if (o.bool("active") == false) Tint.grayBg else Tint.indigoBg, tagFg = if (o.bool("active") == false) Tint.grayFg else Tint.indigoFg,
                    meta = o.obj("category")?.str("description"),
                    details = listOfNotNull("Prioridade" to prLabel, "Categoria" to (o.obj("category")?.str("description") ?: "—"), "Departamento" to (o.obj("department")?.str("name") ?: "—"), "Só horário comercial" to (if (o.bool("only_business_hours") == true) "Sim" else "Não")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Cadências —————

@Composable
private fun CadencesModule(nav: ModuleNav) {
    ModuleListScreen(title = "Cadências", subtitle = "Sequências de toques", onBack = nav.onBack) { _, _, skip, take ->
        apiGet("cadence", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: "Cadência", subtitle = o.str("description"),
                    tag = if (o.bool("is_active") == true) "Ativa" else "Inativa", tagBg = if (o.bool("is_active") == true) Tint.greenBg else Tint.grayBg, tagFg = if (o.bool("is_active") == true) Tint.greenFg else Tint.grayFg,
                    meta = "${o.int("steps_count") ?: 0} etapas · ${o.int("enrollments_count") ?: 0} inscritos",
                    details = listOfNotNull("Alvo" to (o.str("target_type") ?: "—"), "Sai ao ganhar" to (if (o.bool("exit_on_won") == true) "Sim" else "Não"), "Sai ao perder" to (if (o.bool("exit_on_lost") == true) "Sim" else "Não"), "Sai ao responder" to (if (o.bool("exit_on_reply") == true) "Sim" else "Não")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Discador —————

@Composable
private fun DialerModule(nav: ModuleNav) {
    ModuleListScreen(
        title = "Discador", subtitle = "Campanhas de ligação", onBack = nav.onBack,
        actions = { row -> when (row.raw?.str("status")) {
            "running", "active" -> listOf(ModuleAction("Pausar campanha") { r -> apiPost("dialer/campaigns/${r.id}/pause").map { "Campanha pausada" } })
            "paused", "draft", "scheduled" -> listOf(ModuleAction("Iniciar campanha", VdButtonStyle.Primary) { r -> apiPost("dialer/campaigns/${r.id}/start").map { "Campanha iniciada" } })
            else -> emptyList()
        } }
    ) { _, _, skip, take ->
        apiGet("dialer/campaigns", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "running", "active" -> Triple("Em andamento", Tint.blueBg, Tint.blueFg); "paused" -> Triple("Pausada", Tint.yellowBg, Tint.yellowFg); "finished", "completed" -> Triple("Concluída", Tint.greenBg, Tint.greenFg); "canceled", "cancelled" -> Triple("Cancelada", Tint.redBg, Tint.redFg); else -> Triple("Rascunho", Tint.grayBg, Tint.grayFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: "Campanha", subtitle = o.str("description"),
                    tag = label, tagBg = bg, tagFg = fg, meta = "${o.int("total_items") ?: 0} contatos",
                    details = listOfNotNull("Janela" to "${o.str("window_start") ?: "—"} – ${o.str("window_end") ?: "—"}", "Tentativas" to (o.int("max_attempts")?.toString() ?: "—"), "Estratégia" to (o.str("distribution_strategy") ?: "—"), "Início" to (fmtDate(o.str("started_at")) ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Orçamentos e contratos —————

@Composable
private fun QuotesModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Orçamentos e contratos", onBack = nav.onBack, filters = listOf("Todos", "Rascunho", "Enviados", "Aceitos", "Rejeitados"),
        actions = { row -> buildList {
            val st = row.raw?.str("status")
            row.raw?.str("pdf_url")?.let { url -> add(ModuleAction("Abrir PDF", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo PDF") }) }
            if (st == "draft") add(ModuleAction("Marcar como enviado") { r -> apiPut("quote/${r.id}/sent").map { "Marcado como enviado" } })
            if (st == "sent") { add(ModuleAction("Marcar como aceito", VdButtonStyle.Primary) { r -> apiPut("quote/${r.id}/accepted").map { "Marcado como aceito" } }); add(ModuleAction("Marcar como rejeitado", VdButtonStyle.Destructive) { r -> apiPut("quote/${r.id}/rejected").map { "Marcado como rejeitado" } }) }
            row.raw?.int("deal_id")?.let { did -> add(ModuleAction("Abrir negócio") { Result.success("open-deal:$did") }) }
        } }
    ) { _, f, skip, take ->
        val params = mutableMapOf("take" to take.toString(), "skip" to skip.toString())
        when (f) { "Rascunho" -> params["status"] = "draft"; "Enviados" -> params["status"] = "sent"; "Aceitos" -> params["status"] = "accepted"; "Rejeitados" -> params["status"] = "rejected" }
        apiGet("quote", params).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "sent" -> Triple("Enviado", Tint.blueBg, Tint.blueFg); "accepted" -> Triple("Aceito", Tint.greenBg, Tint.greenFg); "rejected" -> Triple("Rejeitado", Tint.redBg, Tint.redFg); "expired" -> Triple("Expirado", Tint.grayBg, Tint.grayFg); else -> Triple("Rascunho", Tint.yellowBg, Tint.yellowFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = "Orçamento ${o.str("number") ?: o.int("id")}",
                    subtitle = listOfNotNull(o.obj("contact")?.str("name") ?: o.obj("client")?.str("name"), o.str("notes")?.take(60)).joinToString(" · "),
                    tag = label, tagBg = bg, tagFg = fg, meta = fmtMoney(o.dbl("total")),
                    details = listOfNotNull("Total" to fmtMoney(o.dbl("total")), "Desconto" to fmtMoney(o.dbl("discount")), "Válido até" to (fmtDate(o.str("valid_until"), "dd/MM/yyyy") ?: "—"), "Enviado em" to (fmtDate(o.str("sent_at")) ?: "—"), "Aceito em" to (fmtDate(o.str("accepted_at")) ?: "—"), "Revisão" to (o.int("revision")?.toString() ?: "—")),
                    body = o.str("notes"), raw = o
                )
            }.let { rows -> if (f == "Todos") rows else rows.filter { it.tag == when (f) { "Rascunho" -> "Rascunho"; "Enviados" -> "Enviado"; "Aceitos" -> "Aceito"; else -> "Rejeitado" } } }, total)
        }
    }
}

// ————— Landing pages · Link na bio —————

@Composable
private fun LandingPagesModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Landing pages", subtitle = "Páginas e link na bio", onBack = nav.onBack,
        actions = { row -> listOfNotNull((row.raw?.str("public_url") ?: row.raw?.str("url"))?.let { url -> ModuleAction("Abrir página", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo página") } }) }
    ) { _, _, skip, take ->
        apiGet("landing-page", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val published = o.bool("is_published") == true || o.str("status") == "published"
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("title") ?: o.str("name") ?: "Página",
                    subtitle = o.str("slug")?.let { "/$it" } ?: o.str("public_url"),
                    tag = if (published) "Publicada" else "Rascunho", tagBg = if (published) Tint.greenBg else Tint.grayBg, tagFg = if (published) Tint.greenFg else Tint.grayFg,
                    meta = o.int("views")?.let { "$it visitas" } ?: o.int("visits_count")?.let { "$it visitas" },
                    details = listOfNotNull("Leads" to (o.int("leads_count")?.toString() ?: o.int("conversions")?.toString() ?: "—"), "Atualizada" to (fmtDate(o.str("updated_at")) ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Pipelines · Produtos · Motivos de perda (leitura) —————

@Composable
private fun PipelinesConfigModule(nav: ModuleNav) {
    ModuleListScreen(title = "Configuração do CRM", subtitle = "Somente leitura · edição no desktop", onBack = nav.onBack, filters = listOf("Pipelines", "Produtos", "Motivos de perda")) { _, f, skip, take ->
        when (f) {
            "Produtos" -> apiGet("product", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
                val (list, total) = body.rows()
                ModulePage(list.map { o -> ModuleRow(id = "p-${o.int("id")}", title = o.str("name") ?: "Produto", subtitle = o.str("description")?.take(100), meta = fmtMoney(o.dbl("price") ?: o.dbl("value")), tag = if (o.bool("active") == false) "Inativo" else null, raw = o) }, total)
            }
            "Motivos de perda" -> apiGet("loss-reason", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
                val (list, total) = body.rows()
                ModulePage(list.map { o -> ModuleRow(id = "l-${o.int("id")}", title = o.str("name") ?: o.str("description") ?: "Motivo", raw = o) }, total)
            }
            else -> if (skip > 0) Result.success(ModulePage(emptyList(), null)) else apiGet("pipeline").map { body ->
                val (list, _) = body.rows()
                ModulePage(list.map { o ->
                    val stages = o.arr("stages")?.objects()?.mapNotNull { it.str("name") }.orEmpty()
                    ModuleRow(id = "pl-${o.int("id")}", title = o.str("name") ?: "Pipeline", subtitle = if (stages.isEmpty()) null else stages.joinToString(" › "), meta = "${stages.size} etapas", details = stages.mapIndexed { i, s -> "Etapa ${i + 1}" to s }, raw = o)
                }, null)
            }
        }
    }
}

// ————— Cobranças (links de pagamento) —————

@Composable
private fun BillingModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Cobranças", subtitle = "Links de pagamento", onBack = nav.onBack, filters = listOf("Todas", "Pendentes", "Pagas", "Canceladas"),
        emptyTitle = "Nenhuma cobrança", emptySubtitle = "Links de pagamento gerados nas conversas aparecem aqui.",
        actions = { row -> buildList {
            (row.raw?.str("url") ?: row.raw?.str("payment_url") ?: row.raw?.str("link"))?.let { url -> add(ModuleAction("Abrir link", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo link") }) }
            add(ModuleAction("Sincronizar status") { r -> apiPost("payment-link/${r.id}/sync").map { "Status sincronizado" } })
            if (row.raw?.str("status") in listOf("pending", "waiting", "open")) add(ModuleAction("Cancelar cobrança", VdButtonStyle.Destructive) { r -> apiPost("payment-link/${r.id}/cancel").map { "Cobrança cancelada" } })
        } }
    ) { _, f, skip, take ->
        val page = skip / take + 1
        val params = mutableMapOf("per_page" to take.toString(), "page" to page.toString(), "take" to take.toString())
        when (f) { "Pendentes" -> params["status"] = "pending"; "Pagas" -> params["status"] = "paid"; "Canceladas" -> params["status"] = "canceled" }
        apiGet("payment-link", params).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (label, bg, fg) = when (st) { "paid" -> Triple("Paga", Tint.greenBg, Tint.greenFg); "canceled", "cancelled" -> Triple("Cancelada", Tint.redBg, Tint.redFg); "expired" -> Triple("Expirada", Tint.grayBg, Tint.grayFg); else -> Triple("Pendente", Tint.yellowBg, Tint.yellowFg) }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("description") ?: o.str("title") ?: "Cobrança ${o.int("id")}",
                    subtitle = listOfNotNull(o.obj("contact")?.str("name") ?: o.str("contact_name"), o.str("provider") ?: o.str("gateway")).joinToString(" · "),
                    tag = label, tagBg = bg, tagFg = fg, meta = fmtMoney(o.dbl("amount") ?: o.dbl("value")),
                    details = listOfNotNull("Valor" to fmtMoney(o.dbl("amount") ?: o.dbl("value")), "Método" to (o.str("payment_method") ?: o.str("method") ?: "—"), "Vencimento" to (fmtDate(o.str("due_date"), "dd/MM/yyyy") ?: fmtDate(o.str("expires_at")) ?: "—"), "Pago em" to (fmtDate(o.str("paid_at")) ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Confirmações de agendamento (tela 24) —————

@Composable
private fun ConfirmationsModule(nav: ModuleNav) {
    var version by remember { mutableStateOf(0) }
    ModuleListScreen(
        title = "Confirmações", subtitle = "Agendamentos aguardando confirmação", onBack = nav.onBack, reloadKey = version,
        filters = listOf("Próximos 7 dias", "Próximos 30 dias"),
        emptyTitle = "Tudo confirmado", emptySubtitle = "Nenhum agendamento pendente no período.",
        actions = { row -> buildList {
            add(ModuleAction("Confirmar", VdButtonStyle.Primary) { r -> apiPut("appointments/${r.id}/confirm").map { version++; "Agendamento confirmado" } })
            add(ModuleAction("Lembrar pelo WhatsApp") { r ->
                val cid = r.raw?.int("contact_id") ?: return@ModuleAction Result.failure(Exception("Agendamento sem contato"))
                runCatching {
                    val convId = br.com.vipdesk.mobile.di.AppContainer.crmRepository.getContactConversationId(cid).getOrThrow() ?: throw Exception("Contato ainda não possui conversa")
                    val whenTxt = fmtDate(r.raw?.str("start_date"), "dd/MM 'às' HH:mm") ?: ""
                    val msg = "Olá${r.raw?.str("contact")?.let { ", $it" } ?: ""}! Lembrando do seu agendamento${r.raw?.str("service")?.let { " de $it" } ?: ""} em $whenTxt. Podemos confirmar?"
                    br.com.vipdesk.mobile.di.AppContainer.conversationRepository.sendTextMessage(convId, msg).getOrThrow()
                    "Lembrete enviado no WhatsApp"
                }
            })
            add(ModuleAction("Cancelar agendamento", VdButtonStyle.Destructive) { r -> apiPut("appointments/${r.id}/cancel").map { version++; "Agendamento cancelado" } })
            row.raw?.int("contact_id")?.let { cid -> add(ModuleAction("Abrir contato") { Result.success("open-contact:$cid") }) }
        } }
    ) { _, f, skip, _ ->
        if (skip > 0) Result.success(ModulePage(emptyList(), null)) else {
            val days = if (f == "Próximos 30 dias") 30 else 7
            val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("pt", "BR"))
            val cal = java.util.Calendar.getInstance()
            val start = fmt.format(cal.time); cal.add(java.util.Calendar.DAY_OF_YEAR, days); val end = fmt.format(cal.time)
            apiGet("appointments/range", mapOf("start_date" to start, "end_date" to end)).map { body ->
                val (list, _) = body.rows()
                ModulePage(list.filter { it.str("status") == "pending" }.map { o ->
                    ModuleRow(
                        id = o.int("id").toString(), title = o.str("contact") ?: o.str("phone_number") ?: "Agendamento",
                        subtitle = listOfNotNull(o.str("service"), o.str("professional")).joinToString(" · "),
                        tag = "Aguard. confirmação", tagBg = Tint.yellowBg, tagFg = Tint.yellowFg,
                        meta = fmtDate(o.str("start_date"), "EEE dd/MM HH:mm"), avatar = o.str("contact"),
                        details = listOfNotNull("Início" to (fmtDate(o.str("start_date")) ?: "—"), "Fim" to (fmtDate(o.str("end_date"), "HH:mm") ?: "—"), "Telefone" to (o.str("phone_number") ?: "—"), "Pagamento" to (o.str("payment_status") ?: "—"), "Observações" to (o.str("notes") ?: "—")),
                        raw = o
                    )
                }, null)
            }
        }
    }
}

// ————— Administração (empresas) —————

@Composable
private fun AdminModule(nav: ModuleNav) {
    ModuleListScreen(title = "Administração", subtitle = "Empresas da conta", onBack = nav.onBack, searchHint = "Buscar empresa") { q, _, skip, take ->
        apiGet("company", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val blocked = o.bool("blocked") == true
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name")?.trim() ?: "Empresa ${o.int("id")}",
                    subtitle = listOfNotNull(o.str("email"), o.str("user")?.let { "resp. $it" }).joinToString(" · "),
                    tag = if (blocked) "Bloqueada" else if (o.bool("paid_account") == true) "Pagante" else "Trial", tagBg = if (blocked) Tint.redBg else if (o.bool("paid_account") == true) Tint.greenBg else Tint.yellowBg, tagFg = if (blocked) Tint.redFg else if (o.bool("paid_account") == true) Tint.greenFg else Tint.yellowFg,
                    meta = o.str("created_at"),
                    details = listOfNotNull("ID" to (o.int("id")?.toString() ?: "—"), "Criada em" to (o.str("created_at") ?: "—"), "Parceiro" to (o.str("partner") ?: "—"), "DeskFlow" to (if (o.bool("use_deskflow") == true) "Sim" else "Não"), "Pós-pago" to (if (o.bool("postpaid") == true) "Sim" else "Não"), "Telefone" to (o.str("phone_optional") ?: "—"), "Último ticket" to (o.str("last_ticket_number") ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Observabilidade (segurança e acessos) —————

@Composable
private fun ObservabilityModule(nav: ModuleNav) {
    ModuleListScreen(title = "Observabilidade", subtitle = "Segurança, acessos e eventos", onBack = nav.onBack, filters = listOf("Resumo", "Eventos", "Online")) { _, f, skip, take ->
        if (skip > 0 && f != "Eventos") Result.success(ModulePage(emptyList(), null)) else when (f) {
            "Eventos" -> apiGet("security/events", mapOf("take" to take.toString(), "skip" to skip.toString(), "per_page" to take.toString(), "page" to (skip / take + 1).toString())).map { body ->
                val (list, total) = body.rows()
                ModulePage(list.map { o ->
                    val sev = o.str("severity")
                    val (bg, fg) = when (sev?.uppercase()) { "CRITICAL", "ERROR" -> Tint.redBg to Tint.redFg; "WARNING" -> Tint.yellowBg to Tint.yellowFg; else -> Tint.grayBg to Tint.grayFg }
                    ModuleRow(id = (o.int("id") ?: o.hashCode()).toString(), title = o.str("event_type") ?: o.str("type") ?: "Evento", subtitle = listOfNotNull(o.str("user_name") ?: o.obj("user")?.str("name"), o.str("company_name"), o.str("ip_address")).joinToString(" · "), tag = sev, tagBg = bg, tagFg = fg, meta = fmtRelative(o.str("created_at")), body = o.str("description") ?: o.get("details")?.toString(), raw = o)
                }, total)
            }
            "Online" -> apiGet("security/online-users").map { body ->
                val (list, _) = body.rows("data", "users")
                ModulePage(list.map { o -> ModuleRow(id = (o.int("id") ?: o.hashCode()).toString(), title = o.str("name") ?: "Usuário", subtitle = listOfNotNull(o.str("email"), o.str("last_client_version")?.let { "v$it" }).joinToString(" · "), meta = fmtRelative(o.str("last_seen")), avatar = o.str("name"), tag = o.str("status"), raw = o) }, null)
            }
            else -> apiGet("security/dashboard").map { body ->
                val o = body.asJsonObject
                val rows = listOf(
                    ModuleRow(id = "mfa", title = "MFA habilitado", subtitle = "${o.int("mfa_enabled") ?: 0} de ${o.int("mfa_total_users") ?: 0} usuários", meta = "${o.dbl("mfa_percent")?.toInt() ?: 0}%", leftBorder = if ((o.dbl("mfa_percent") ?: 0.0) >= 80) Tint.greenFg else Tint.amberFg),
                    ModuleRow(id = "logins", title = "Tentativas de login", subtitle = "${o.int("success_attempts") ?: 0} com sucesso · ${o.int("failed_attempts") ?: 0} falharam", meta = "${o.int("total_attempts") ?: 0}", leftBorder = if ((o.int("failed_attempts") ?: 0) > 0) Tint.amberFg else Tint.greenFg),
                    ModuleRow(id = "ips", title = "IPs bloqueados", meta = "${o.int("blocked_ips") ?: 0}", leftBorder = if ((o.int("blocked_ips") ?: 0) > 0) Tint.redFg else Tint.greenFg),
                    ModuleRow(id = "critical", title = "Eventos críticos", meta = "${o.int("critical_events") ?: 0}", leftBorder = if ((o.int("critical_events") ?: 0) > 0) Tint.redFg else Tint.greenFg)
                ) + (o.arr("recent_events")?.objects().orEmpty().take(10).map { e ->
                    ModuleRow(id = "ev-${e.int("id") ?: e.hashCode()}", title = e.str("event_type") ?: "Evento", subtitle = listOfNotNull(e.str("email"), e.str("ip_address")).joinToString(" · "), tag = e.str("severity"), meta = fmtRelative(e.str("created_at")), raw = e)
                })
                ModulePage(rows, null)
            }
        }
    }
}

// ————— Plano (faturas) —————

@Composable
private fun PlanModule(nav: ModuleNav) {
    val context = LocalContext.current
    ModuleListScreen(
        title = "Plano", subtitle = "Faturas e pagamentos", onBack = nav.onBack, searchHint = "Empresa ou nº da fatura",
        filters = listOf("Todas", "Em aberto", "Pagas", "Vencidas"),
        emptyTitle = "Sem faturas", emptySubtitle = "Nenhuma fatura encontrada.",
        actions = { row -> listOfNotNull(
            row.raw?.str("invoice_url")?.let { url -> ModuleAction("Abrir fatura", VdButtonStyle.Primary) { openUrl(context, url); Result.success("Abrindo fatura") } },
            row.raw?.str("barcode_url")?.let { url -> ModuleAction("Boleto / código de barras") { openUrl(context, url); Result.success("Abrindo boleto") } }
        ) }
    ) { q, f, skip, take ->
        apiGet("company-pricing-plan", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            val rows = list.map { o ->
                val st = o.str("status") ?: "—"
                val (bg, fg) = when { st.contains("pag", true) -> Tint.greenBg to Tint.greenFg; st.contains("venc", true) || st.contains("atras", true) -> Tint.redBg to Tint.redFg; st.contains("aberto", true) -> Tint.yellowBg to Tint.yellowFg; else -> Tint.grayBg to Tint.grayFg }
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("company_name")?.trim() ?: "Fatura ${o.str("invoice_number")}",
                    subtitle = listOfNotNull("Fatura ${o.str("invoice_number") ?: o.int("id")}", o.obj("plan")?.str("name"), o.str("payment_type")).joinToString(" · "),
                    tag = st, tagBg = bg, tagFg = fg, meta = fmtMoney(o.dbl("total_value")),
                    details = listOfNotNull("Valor" to fmtMoney(o.dbl("total_value")), "Pago" to fmtMoney(o.dbl("total_paid")), "Vencimento" to (o.str("due_date") ?: "—"), "Pago em" to (o.str("paid_at") ?: "—"), "Vigência" to "${o.str("activated_at") ?: "—"} a ${o.str("valid_until") ?: "—"}", "Forma" to (o.str("payment_type") ?: "—")),
                    raw = o
                )
            }
            val filtered = when (f) { "Em aberto" -> rows.filter { it.tag?.contains("aberto", true) == true }; "Pagas" -> rows.filter { it.tag?.contains("pag", true) == true }; "Vencidas" -> rows.filter { it.tag?.contains("venc", true) == true }; else -> rows }
            ModulePage(filtered, if (f == "Todas") total else null)
        }
    }
}

// ————— Base Legal (add-on) —————

@Composable
private fun BaseLegalModule(nav: ModuleNav) {
    ModuleListScreen(title = "Base Legal", subtitle = "Lotes de documentos indexados", onBack = nav.onBack, emptyTitle = "Nenhum lote", emptySubtitle = "Envie documentos pela versão web para indexar.") { _, _, skip, take ->
        apiGet("base-legal/batches", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                val st = o.str("status")
                val (bg, fg) = statusTint(st)
                ModuleRow(id = o.int("id").toString(), title = o.str("name") ?: o.str("title") ?: "Lote ${o.int("id")}", subtitle = listOfNotNull(o.int("files_count")?.let { "$it arquivos" }, o.int("chunks_count")?.let { "$it trechos" }).joinToString(" · "), tag = st, tagBg = bg, tagFg = fg, meta = fmtRelative(o.str("created_at")), body = o.str("description"), raw = o)
            }, total)
        }
    }
}

// ————— Customer Success · carteira de clientes —————

@Composable
private fun ClientsModule(nav: ModuleNav) {
    ModuleListScreen(title = "Carteira de clientes", subtitle = "Customer Success", onBack = nav.onBack, searchHint = "Nome, CNPJ ou e-mail") { q, _, skip, take ->
        apiGet("client", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o ->
                ModuleRow(
                    id = o.int("id").toString(), title = o.str("name") ?: o.str("corporate_name") ?: "Cliente",
                    subtitle = listOfNotNull(o.str("cnpj") ?: o.str("cpf_cnpj"), o.str("email"), o.str("phone_number")).joinToString(" · "),
                    tag = if (o.bool("active") == false) "Inativo" else null, avatar = o.str("name"),
                    meta = o.int("contacts_count")?.let { "$it contatos" },
                    details = listOfNotNull("Razão social" to (o.str("corporate_name") ?: "—"), "Cidade" to (listOfNotNull(o.str("city"), o.str("state")).joinToString("/").ifBlank { "—" }), "Segmento" to (o.str("segment") ?: "—"), "Cliente desde" to (fmtDate(o.str("created_at"), "dd/MM/yyyy") ?: "—")),
                    raw = o
                )
            }, total)
        }
    }
}

// ————— Metas · Comissões —————

@Composable
private fun GoalsModule(nav: ModuleNav) {
    ModuleListScreen(title = "Metas e comissões", onBack = nav.onBack, filters = listOf("Metas", "Comissões"), emptyTitle = "Nada cadastrado", emptySubtitle = "Configure metas e regras de comissão na versão web.") { _, f, skip, take ->
        if (f == "Comissões") apiGet("commissions", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val o = body.takeIf { it.isJsonObject }?.asJsonObject
            val (list, _) = body.rows()
            val summary = o?.obj("summary")
            val header = summary?.let { s -> ModuleRow(id = "summary", title = "Resumo do período", subtitle = "${s.int("count") ?: 0} negócios · base ${fmtMoney(s.dbl("base_total"))}", meta = fmtMoney(s.dbl("commission_total")), leftBorder = Tint.greenFg) }
            ModulePage(listOfNotNull(header) + list.map { c -> ModuleRow(id = c.int("id").toString(), title = c.obj("user")?.str("name") ?: c.str("user_name") ?: "Vendedor", subtitle = c.obj("deal")?.str("title") ?: c.str("deal_title"), meta = fmtMoney(c.dbl("commission") ?: c.dbl("amount")), tag = c.str("status"), raw = c) }, null)
        } else apiGet("sales-goal", mapOf("take" to take.toString(), "skip" to skip.toString())).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { g ->
                val target = g.dbl("target") ?: g.dbl("value") ?: g.dbl("goal"); val achieved = g.dbl("achieved") ?: g.dbl("current")
                val pct = if (target != null && target > 0 && achieved != null) (achieved / target * 100).toInt() else null
                ModuleRow(id = g.int("id").toString(), title = g.obj("user")?.str("name") ?: g.str("name") ?: "Meta", subtitle = listOfNotNull(g.str("period") ?: g.str("month"), pct?.let { "$it% atingido" }).joinToString(" · "), meta = fmtMoney(target), tag = pct?.let { if (it >= 100) "Batida" else "Em andamento" }, tagBg = if ((pct ?: 0) >= 100) Tint.greenBg else Tint.yellowBg, tagFg = if ((pct ?: 0) >= 100) Tint.greenFg else Tint.yellowFg, raw = g)
            }, total)
        }
    }
}


// ————— Respostas rápidas (fast-message) —————

@Composable
private fun QuickRepliesModule(nav: ModuleNav) {
    var showNew by remember { mutableStateOf(false) }
    var version by remember { mutableStateOf(0) }
    if (showNew) TextPromptSheet(
        title = "Nova resposta rápida", hint = "Primeira linha = título; restante = mensagem. Use {contact_name} e {greeting}.",
        confirmLabel = "Salvar", minLines = 4, onDismiss = { showNew = false }
    ) { text ->
        val lines = text.lines()
        val title = lines.first().trim().take(80)
        val message = lines.drop(1).joinToString("\n").trim().ifBlank { title }
        apiPost("fast-message", json("title" to title, "message" to message, "fast_menu" to 1)).map { version++; "Resposta rápida criada" }
    }
    ModuleListScreen(
        title = "Respostas rápidas", subtitle = "Disponíveis no botão Templates do chat", onBack = nav.onBack, reloadKey = version, searchHint = "Buscar",
        emptyTitle = "Nenhuma resposta rápida", emptySubtitle = "Toque em + para criar.",
        headerActions = { br.com.vipdesk.mobile.ui.components.VdHeaderIcon(Icons.Outlined.AddCircleOutline, "Nova resposta", { showNew = true }) },
        actions = { listOf(ModuleAction("Excluir", VdButtonStyle.Destructive) { r -> apiDelete("fast-message/${r.id}").map { "Resposta excluída" } }) }
    ) { q, _, skip, take ->
        apiGet("fast-message", pageParams(q, skip, take)).map { body ->
            val (list, total) = body.rows()
            ModulePage(list.map { o -> ModuleRow(id = o.int("id").toString(), title = o.str("title") ?: "#${o.int("id")}", subtitle = o.str("message")?.take(140), body = o.str("message"), tag = if (o.bool("fast_menu") == true) "No menu do chat" else null, tagBg = Tint.greenBg, tagFg = Tint.greenFg, raw = o) }, total)
        }
    }
}
