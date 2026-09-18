package br.com.vipdesk.mobile.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import br.com.vipdesk.mobile.ui.modules.rows
import br.com.vipdesk.mobile.ui.modules.bool
import br.com.vipdesk.mobile.ui.modules.str
import br.com.vipdesk.mobile.ui.modules.int
import br.com.vipdesk.mobile.ui.modules.objects
import br.com.vipdesk.mobile.ui.modules.arr
import br.com.vipdesk.mobile.ui.modules.obj
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.data.model.TimelineEvent
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTabs
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusTint
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Inter
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay


/** Detalhe do ticket (tela 15): cabeçalho compacto, SLA, abas Atividades / Propriedades / Horas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    onBack: () -> Unit,
    onOpenContact: (Int) -> Unit,
    viewModel: TicketDetailViewModel,
    onOpenConversation: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val t = uiState.ticket
    var tab by remember { mutableStateOf("Atividades") }
    var showResolve by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.message) { uiState.message?.let { toast = it; viewModel.clearMessage() } }
    LaunchedEffect(toast) { if (toast != null) { delay(2000); toast = null } }

    if (showResolve) {
        AlertDialog(
            onDismissRequest = { showResolve = false },
            containerColor = c.surface,
            title = { Text("Finalizar ticket", color = c.text, fontWeight = FontWeight.SemiBold) },
            text = { Text("Deseja marcar este ticket como resolvido?", color = c.muted) },
            confirmButton = {
                TextButton(onClick = { showResolve = false; viewModel.resolve() }) {
                    Text("Finalizar", color = Tint.greenFg, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { showResolve = false }) { Text("Cancelar", color = c.muted) } }
        )
    }

    if (uiState.showTransferDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissTransferDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                Text("Transferir para…", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                if (uiState.agents.isEmpty()) Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp))
                }
                uiState.agents.forEach { agent ->
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.transferTo(agent.id) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VdAvatar(name = agent.name, size = 34.dp, fontSize = 12, agent = true)
                        Column { Text(agent.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.text); Text(agent.email, fontSize = 11.5.sp, color = c.muted) }
                    }
                    HorizontalDivider(color = c.divider)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            VdSubHeader(
                title = "Tickets › ${t?.ticketNumber?.let { "#$it" } ?: "#${t?.id ?: ""}"}",
                breadcrumb = true,
                onBack = onBack,
                actions = {
                    if (uiState.isActing) { CircularProgressIndicator(Modifier.size(16.dp), color = c.primary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                    VdHeaderIcon(Icons.Default.MoreVert, "Ações", { showActions = true })
                },
                below = if (t == null) null else {
                    {
                        val (stLabel, _) = ticketStatusVisual(t.status, t.isOpen)
                        val (stBg, stFg) = ticketStatusTint(stLabel)
                        val (priLabel, priColor) = ticketPriorityVisual(t.priority)
                        Text(t.title ?: "Sem título", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, lineHeight = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            VdTag(stLabel, color = stFg, background = stBg)
                            Row(
                                Modifier.background(priColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(Modifier.size(6.dp).background(priColor, CircleShape))
                                Text(priLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = priColor)
                            }
                            t.channel?.let { VdTag(sourceLabel(it)) }
                            t.department?.let { VdTag(it) }
                            Spacer(Modifier.weight(1f))
                            t.createdAt?.let { Text("criado ${relativeTime(it)}", fontSize = 10.sp, color = c.muted) }
                        }
                        t.sla?.let { sla ->
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(if (sla.within) c.surface else Tint.roseBg)
                                    .border(1.dp, if (sla.within) c.divider else Color(0xFFFECACA), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("SLA${sla.name?.let { " · $it" } ?: ""}".uppercase(), fontSize = 9.sp, color = c.muted)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Outlined.Timer, null, tint = if (sla.within) Tint.amberFg else Tint.redFg, modifier = Modifier.size(14.dp))
                                        Text(sla.label.ifBlank { if (sla.within) "dentro do prazo" else "violado" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (sla.within) Tint.amberFg else Tint.redFg)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        VdTabs(listOf("Atividades", "Propriedades", "Horas"), tab, { tab = it }, modifier = Modifier.padding(horizontal = 0.dp))
                    }
                }
            )

            when {
                uiState.isLoading && t == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                t == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(uiState.error ?: "Ticket não encontrado", color = c.muted) }
                else -> when (tab) {
                    "Atividades" -> ActivitiesTab(uiState, viewModel, onResolve = { showResolve = true }, onToast = { toast = it })
                    "Propriedades" -> PropertiesTab(uiState, viewModel, onOpenContact, onResolve = { showResolve = true }, onToast = { toast = it })
                    else -> HoursTab(ticketId = uiState.ticket?.id ?: 0, onToast = { toast = it })
                }
            }
        }
        toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp)) { VdToast(it) } }
    }

    if (showActions && t != null) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 4.dp).padding(bottom = 24.dp)) {
                VdSheetRow(Icons.Outlined.PersonAddAlt, "Atribuir a mim", onClick = { showActions = false; viewModel.assignToMe() })
                VdSheetRow(Icons.Outlined.SwapHoriz, "Transferir para outro agente", onClick = { showActions = false; viewModel.openTransferDialog() })
                if (t.isOpen) {
                    HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    VdSheetRow(Icons.Outlined.CheckCircle, "Finalizar ticket", iconTint = Tint.greenFg, textColor = Tint.greenFg, bold = true, onClick = { showActions = false; showResolve = true })
                }
            }
        }
    }
}

// ————— Aba Atividades —————

@Composable
private fun ActivitiesTab(
    state: TicketDetailUiState,
    viewModel: TicketDetailViewModel,
    onResolve: () -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val t = state.ticket ?: return
    var internal by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp)
        ) {
            // Timeline (eventos da API + comentários, em ordem)
            val events = t.timeline.map { TimelineEntry(it.createdAt, it.user, it.message, it.type, false) } +
                state.comments.map { TimelineEntry(it.createdAt, it.user?.name, it.message ?: "", "comment", true) }
            val sorted = events.sortedBy { it.at ?: "" }
            if (sorted.isEmpty()) {
                Text("Sem atividades registradas.", fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(vertical = 12.dp))
            }
            sorted.forEachIndexed { i, e ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.padding(top = 4.dp).size(10.dp)
                                .background(if (e.internal) c.warning else c.primary, CircleShape)
                        )
                        if (i < sorted.lastIndex) Box(Modifier.width(1.dp).weight(1f).background(c.divider))
                    }
                    Column(Modifier.weight(1f).padding(bottom = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                listOfNotNull(e.at?.let { relativeTime(it) }, e.user ?: "Sistema").joinToString(" · "),
                                fontSize = 11.sp, color = c.muted
                            )
                            if (e.internal) { Icon(Icons.Outlined.Lock, null, tint = c.muted, modifier = Modifier.size(11.dp)); Text("interno", fontSize = 11.sp, color = c.muted) }
                        }
                        if (e.internal) {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 3.dp).background(c.internalBubble, RoundedCornerShape(8.dp))
                                    .border(1.dp, c.internalBubbleBorder, RoundedCornerShape(8.dp)).padding(8.dp)
                            ) { Text(e.text, fontSize = 12.sp, lineHeight = 17.sp, color = c.text) }
                        } else {
                            Text(e.text, fontSize = 12.sp, lineHeight = 17.sp, color = c.text)
                        }
                    }
                }
            }
        }

        // Composer fixo (Responder / Interno / Finalizar)
        Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding().imePadding()) {
            HorizontalDivider(color = c.divider)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                ComposerChip("Responder", selected = !internal, onClick = {
                    // Resposta ao cliente acontece na conversa vinculada ao ticket
                    onToast("Responda ao cliente pela conversa vinculada")
                    internal = false
                })
                ComposerChip("Interno", selected = internal, icon = Icons.Outlined.Lock, onClick = { internal = true })
                Spacer(Modifier.weight(1f))
                if (t.isOpen) {
                    Text(
                        "Finalizar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tint.greenFg,
                        modifier = Modifier.border(1.dp, c.success, RoundedCornerShape(12.dp)).clickable(onClick = onResolve).padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    Modifier.weight(1f).heightIn(min = 40.dp).clip(RoundedCornerShape(20.dp))
                        .background(if (internal) c.internalComposer else c.surface)
                        .border(1.dp, if (internal) c.warning else c.border, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (state.commentText.isEmpty()) Text(if (internal) "Comentário interno…" else "Responda pela conversa do cliente", fontSize = 12.sp, color = c.placeholder)
                    BasicTextField(
                        value = state.commentText, onValueChange = viewModel::onCommentTextChange,
                        enabled = internal, maxLines = 4,
                        textStyle = TextStyle(fontSize = 13.sp, color = c.text, fontFamily = Inter),
                        cursorBrush = SolidColor(c.primary), modifier = Modifier.fillMaxWidth()
                    )
                }
                val canSend = internal && state.commentText.isNotBlank()
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(if (canSend) c.primary else c.border)
                        .clickable(enabled = canSend) { viewModel.sendComment() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

private data class TimelineEntry(val at: String?, val user: String?, val text: String, val type: String, val internal: Boolean)

@Composable
private fun ComposerChip(label: String, selected: Boolean, onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val c = AppTheme.colors
    Row(
        Modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) c.primary else c.surface)
            .border(1.dp, if (selected) c.primary else c.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = if (selected) Color.White else c.textTertiary, modifier = Modifier.size(12.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else c.text)
    }
}

// ————— Aba Propriedades —————

@Composable
private fun PropertiesTab(
    state: TicketDetailUiState,
    viewModel: TicketDetailViewModel,
    onOpenContact: (Int) -> Unit,
    onResolve: () -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val t = state.ticket ?: return
    var showStatusSheet by remember { mutableStateOf(false) }
    var showPriority by remember { mutableStateOf(false) }
    var showDepartment by remember { mutableStateOf(false) }
    var departments by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val propScope = rememberCoroutineScope()
    // PUT ticket/{id} faz fill genérico: priority / department_id
    fun updateTicket(field: String, value: Any?, okMsg: String) {
        propScope.launch {
            br.com.vipdesk.mobile.ui.modules.apiPut("ticket/${t.id}", br.com.vipdesk.mobile.ui.modules.json(field to value)).fold(
                { onToast(okMsg); viewModel.load() }, { onToast(it.message ?: "Falha ao atualizar") }
            )
        }
    }
    if (showPriority) br.com.vipdesk.mobile.ui.modules.PickerSheet(
        "Prioridade",
        listOf("very_high" to "Muito alta", "high" to "Alta", "medium" to "Média", "low" to "Baixa", "very_low" to "Muito baixa"),
        selectedId = t.priority, onDismiss = { showPriority = false }
    ) { v -> showPriority = false; updateTicket("priority", v, "Prioridade atualizada") }
    if (showDepartment) {
        LaunchedEffect(Unit) {
            br.com.vipdesk.mobile.ui.modules.apiGet("department", mapOf("take" to "100")).onSuccess { body ->
                departments = body.rows().first.filter { it.bool("active") != false }.map { it.int("id").toString() to (it.str("name") ?: "—") }
            }
        }
        br.com.vipdesk.mobile.ui.modules.PickerSheet("Departamento", departments, onDismiss = { showDepartment = false }) { id ->
            showDepartment = false; updateTicket("department_id", id.toInt(), "Departamento atualizado")
        }
    }
    if (showStatusSheet) {
        TicketStatusSheet(
            statuses = state.statuses,
            currentId = t.statusId,
            onDismiss = { showStatusSheet = false },
            onPick = { status, reason -> showStatusSheet = false; viewModel.changeStatus(status, reason) }
        )
    }
    val (stLabel, _) = ticketStatusVisual(t.status, t.isOpen)
    val (stBg, stFg) = ticketStatusTint(stLabel)
    val (priLabel, priColor) = ticketPriorityVisual(t.priority)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        VdCard(padding = 0.dp) {
            PropRow("Solicitante", t.contact?.name ?: "—", caret = t.contact?.id != null) { t.contact?.id?.let(onOpenContact) }
            PropRow("Status", "", caret = false, trailing = { VdTag("$stLabel ▾", color = stFg, background = stBg) }) { viewModel.loadStatuses(); showStatusSheet = true }
            PropRow("Prioridade", priLabel, dot = priColor) { showPriority = true }
            PropRow("Departamento", t.department ?: "—") { showDepartment = true }
            PropRow("Responsável", t.responsible?.name ?: "sem responsável", avatar = t.responsible?.name) { viewModel.openTransferDialog() }
            PropRow("Canal", sourceLabel(t.channel)) { }
            PropRow("Criado", t.createdAt?.let { relativeTime(it) } ?: "—") { }
            PropRow("Fechado", t.closedAt?.let { relativeTime(it) } ?: "—", last = true) { }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Atribuir a mim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.primary,
                modifier = Modifier.weight(1f).border(1.dp, c.border, RoundedCornerShape(8.dp)).clickable { viewModel.assignToMe() }.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                "Transferir", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text,
                modifier = Modifier.weight(1f).border(1.dp, c.border, RoundedCornerShape(8.dp)).clickable { viewModel.openTransferDialog() }.padding(vertical = 10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PropRow(
    label: String, value: String, caret: Boolean = true, dot: Color? = null, avatar: String? = null,
    last: Boolean = false, trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit
) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).defaultMinSize(minHeight = 44.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = c.muted, modifier = Modifier.weight(1f))
        if (dot != null) { Box(Modifier.size(8.dp).background(dot, CircleShape)); Spacer(Modifier.width(6.dp)) }
        if (avatar != null) { VdAvatar(name = avatar, size = 22.dp, fontSize = 9, agent = true); Spacer(Modifier.width(6.dp)) }
        if (trailing != null) trailing() else Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.text)
        if (caret) Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(18.dp).padding(start = 2.dp))
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}

// ————— Aba Horas —————

@Composable
private fun HoursTab(ticketId: Int, onToast: (String) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<com.google.gson.JsonObject>>(emptyList()) }
    var totals by remember { mutableStateOf<com.google.gson.JsonObject?>(null) }
    var running by remember { mutableStateOf<com.google.gson.JsonObject?>(null) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var version by remember { mutableStateOf(0) }
    var showManual by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(ticketId, version) {
        br.com.vipdesk.mobile.ui.modules.apiGet("time-entry/ticket/$ticketId").onSuccess { body ->
            entries = body.rows().first; totals = body.asJsonObject.obj("totals")
        }
        br.com.vipdesk.mobile.ui.modules.apiGet("time-entry/my-running").onSuccess { body ->
            val o = body.asJsonObject
            running = (o.obj("data") ?: o.arr("data")?.objects()?.firstOrNull())
        }
    }
    LaunchedEffect(running) { while (running != null) { now = System.currentTimeMillis(); kotlinx.coroutines.delay(1000) } }

    val runningHere = running?.int("ticket_id") == ticketId
    val startedMs = running?.str("started_at")?.let { br.com.vipdesk.mobile.ui.common.parseApiDate(it)?.time }
    val elapsed = if (runningHere && startedMs != null) ((now - startedMs) / 1000).coerceAtLeast(0) else 0L
    fun hms(sec: Long) = "%02d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60)

    if (showManual) br.com.vipdesk.mobile.ui.modules.TextPromptSheet(
        title = "Lançar horas", hint = "Minutos trabalhados e descrição. Ex.: 45 Ajuste de configuração", confirmLabel = "Lançar", minLines = 2,
        onDismiss = { showManual = false }
    ) { text ->
        val mins = Regex("^\\s*(\\d+)").find(text)?.groupValues?.get(1)?.toIntOrNull()
        if (mins == null || mins <= 0) Result.failure(Exception("Comece com a quantidade de minutos, ex.: 30 Reunião"))
        else {
            val started = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(System.currentTimeMillis() - mins * 60_000L))
            br.com.vipdesk.mobile.ui.modules.apiPost("time-entry", br.com.vipdesk.mobile.ui.modules.json(
                "ticket_id" to ticketId, "started_at" to started, "duration_minutes" to mins,
                "description" to text.replaceFirst(Regex("^\\s*\\d+\\s*"), "").ifBlank { null }
            )).map { version++; "$mins min lançados" }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF111827)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(if (runningHere) "EM ANDAMENTO" else if (running != null) "CRONÔMETRO ATIVO EM OUTRO TICKET" else "CRONÔMETRO", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                Text(hms(elapsed), fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Box(
                Modifier.size(48.dp).background(if (runningHere) Color(0xFFEF4444) else c.primary, CircleShape).clickable(enabled = !busy) {
                    busy = true
                    scope.launch {
                        val r = if (runningHere) br.com.vipdesk.mobile.ui.modules.apiPost("time-entry/${running?.int("id")}/stop").map { "Worklog finalizado" }
                        else br.com.vipdesk.mobile.ui.modules.apiPost("time-entry/start", br.com.vipdesk.mobile.ui.modules.json("ticket_id" to ticketId)).map { "Cronômetro iniciado" }
                        r.fold({ onToast(it); version++ }, { onToast(it.message ?: "Falha") })
                        busy = false
                    }
                },
                contentAlignment = Alignment.Center
            ) { Text(if (runningHere) "■" else "▶", color = Color.White, fontSize = 18.sp) }
        }
        totals?.let { tt ->
            val total = tt.int("total_minutes") ?: 0; val bill = tt.int("billable_minutes") ?: 0
            Text("Total ${total / 60}h${(total % 60).toString().padStart(2, '0')} · faturável ${bill / 60}h${(bill % 60).toString().padStart(2, '0')}" + ((tt.int("pending_minutes") ?: 0).takeIf { it > 0 }?.let { " · $it min aguardando aprovação" } ?: ""), fontSize = 12.sp, color = c.muted)
        }
        Text("+ lançar horas manualmente", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.primary, modifier = Modifier.clickable { showManual = true })
        if (entries.isEmpty()) Text("Nenhum lançamento neste ticket.", fontSize = 12.sp, color = c.muted)
        entries.forEach { e ->
            val mins = e.int("duration_minutes") ?: 0
            VdCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.obj("user")?.str("name") ?: "—", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                    Text(if (e.str("ended_at") == null) "em curso" else "${mins / 60}h${(mins % 60).toString().padStart(2, '0')}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                }
                Text(listOfNotNull(br.com.vipdesk.mobile.ui.modules.fmtDate(e.str("started_at"), "dd/MM HH:mm"), e.str("activity_type"), e.str("approval_status") ?: e.str("status")).joinToString(" · "), fontSize = 11.sp, color = c.muted)
                e.str("description")?.let { Text(it, fontSize = 12.sp, color = c.text, modifier = Modifier.padding(top = 2.dp)) }
            }
        }
    }
}


/** Sheet de troca de status (Propriedades › Status). Status com pause_sla exige motivo da pendência. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketStatusSheet(
    statuses: List<br.com.vipdesk.mobile.data.model.TicketStatusOption>,
    currentId: Int?,
    onDismiss: () -> Unit,
    onPick: (br.com.vipdesk.mobile.data.model.TicketStatusOption, String?) -> Unit
) {
    val c = AppTheme.colors
    var pending by remember { mutableStateOf<br.com.vipdesk.mobile.data.model.TicketStatusOption?>(null) }
    var reason by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface, dragHandle = { VdSheetHandle() }
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp).imePadding()) {
            val p = pending
            if (p != null) {
                Text("Motivo da pendência", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Text("O status \"${p.description}\" pausa o SLA e exige um motivo.", fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                br.com.vipdesk.mobile.ui.components.VdInput(label = "Motivo", value = reason, onValueChange = { reason = it }, placeholder = "Ex.: aguardando retorno do cliente")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    br.com.vipdesk.mobile.ui.components.VdButton("Voltar", onClick = { pending = null }, style = br.com.vipdesk.mobile.ui.components.VdButtonStyle.Secondary, modifier = Modifier.weight(1f))
                    br.com.vipdesk.mobile.ui.components.VdButton("Aplicar status", onClick = { if (reason.isNotBlank()) onPick(p, reason.trim()) }, modifier = Modifier.weight(1f))
                }
            } else {
                Text("Alterar status", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                if (statuses.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
                }
                Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                    statuses.forEach { st ->
                        val selected = st.id == currentId
                        Row(
                            Modifier.fillMaxWidth().clickable(enabled = !selected) {
                                if (st.pauseSla == true) pending = st else onPick(st, null)
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(st.description ?: "#${st.id}", fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) c.primary else c.text)
                                val meta = listOfNotNull(st.progressPercentage?.let { "$it% concluído" }, if (st.pauseSla == true) "pausa SLA" else null).joinToString(" · ")
                                if (meta.isNotBlank()) Text(meta, fontSize = 11.sp, color = c.muted)
                            }
                            if (selected) Text("atual", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                        }
                        HorizontalDivider(color = c.divider)
                    }
                }
            }
        }
    }
}
