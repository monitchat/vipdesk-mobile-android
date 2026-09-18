package br.com.vipdesk.mobile.ui.crm

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.DealStage
import br.com.vipdesk.mobile.data.model.LossReason
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTabs
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/** Detalhe do negócio (tela 18): valor, stepper de etapas, Ganhar/Perder/Próx. etapa, abas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(dealId: Int, onBack: () -> Unit, onOpenContact: (Int) -> Unit, onOpenConversation: (Int) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var deal by remember { mutableStateOf<ApiDeal?>(null) }
    var stages by remember { mutableStateOf<List<DealStage>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf("Timeline") }
    var showLoss by remember { mutableStateOf(false) }
    var lossReasons by remember { mutableStateOf<List<LossReason>>(emptyList()) }
    var toast by remember { mutableStateOf<String?>(null) }

    suspend fun reload() {
        AppContainer.crmRepository.getDeal(dealId).fold(
            onSuccess = { d ->
                deal = d
                AppContainer.crmRepository.getPipelines().onSuccess { list ->
                    val p = list.find { it.id == (d.pipeline?.id ?: d.pipelineId) } ?: list.find { it.isDefault } ?: list.firstOrNull()
                    stages = p?.stages.orEmpty()
                }
            },
            onFailure = { error = it.message }
        )
        loading = false
    }

    LaunchedEffect(dealId) { reload() }
    var extrasVersion by remember { mutableStateOf(0) }
    val extras = rememberDealExtras(dealId, extrasVersion)
    var activityPrompt by remember { mutableStateOf<String?>(null) }
    activityPrompt?.let { type ->
        br.com.vipdesk.mobile.ui.modules.TextPromptSheet(
            title = if (type == "note") "Nova nota" else "Nova tarefa",
            hint = if (type == "note") "O que aconteceu neste negócio?" else "O que precisa ser feito?",
            confirmLabel = if (type == "note") "Salvar nota" else "Criar tarefa",
            onDismiss = { activityPrompt = null }
        ) { text ->
            val body = br.com.vipdesk.mobile.ui.modules.json(
                "deal_id" to dealId, "type" to type,
                "title" to (if (type == "note") "Nota" else text.lines().first().take(120)),
                "description" to text,
                "completed_at" to (if (type == "note") "now" else null)
            )
            br.com.vipdesk.mobile.ui.modules.apiPost("deal-activity", body).map { extrasVersion++; if (type == "note") "Nota registrada" else "Tarefa criada" }
        }
    }
    LaunchedEffect(toast) { if (toast != null) { delay(2000); toast = null } }

    fun act(block: suspend () -> Result<ApiDeal?>, success: String) {
        busy = true
        scope.launch {
            block().fold(
                onSuccess = { toast = success; CrmEvents.dealsVersion++; reload() },
                onFailure = { toast = it.message ?: "Erro" }
            )
            busy = false
        }
    }

    val d = deal
    val openStages = stages.filter { !it.isWon && !it.isLost }
    val currentIdx = openStages.indexOfFirst { it.id == (d?.stage?.id ?: d?.stageId) }
    val next = openStages.getOrNull(currentIdx + 1)

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            VdSubHeader(
                title = "Negócios › ${d?.pipeline?.name ?: ""}",
                breadcrumb = true,
                onBack = onBack,
                below = if (d == null) null else {
                    {
                        val (stLabel, stBg, stFg) = dealStatusVisual(d.status)
                        Text(d.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = c.text, lineHeight = 21.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(formatDealValue(d.value, d.currency), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                            VdTag(stLabel, color = stFg, background = stBg)
                            d.expectedCloseDate?.let { Text("fecha $it", fontSize = 11.sp, color = c.muted) }
                            Spacer(Modifier.weight(1f))
                            d.owner?.name?.let { VdAvatar(name = it, size = 24.dp, fontSize = 9, agent = true) }
                        }
                        if (openStages.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                openStages.forEachIndexed { i, s ->
                                    val reached = i <= currentIdx
                                    val shape = when (i) {
                                        0 -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 2.dp, bottomEnd = 2.dp)
                                        openStages.lastIndex -> RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                                        else -> RoundedCornerShape(2.dp)
                                    }
                                    Box(
                                        Modifier.weight(1f).height(26.dp).clip(shape)
                                            .background(if (reached) c.primary else c.divider)
                                            .clickable(enabled = d.status == "open" && !busy && i != currentIdx) {
                                                act({ AppContainer.crmRepository.moveDealStage(d.id, s.id) }, "Movido para \"${s.name}\"")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(s.name.take(9), fontSize = 10.sp, fontWeight = if (reached) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (reached) Color.White else c.muted, maxLines = 1)
                                    }
                                }
                            }
                        }
                        if (d.status == "open") {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActionPill("Ganhar", Icons.Outlined.EmojiEvents, c.success, Tint.greenFg, Modifier.weight(1f), enabled = !busy) {
                                    act({ AppContainer.crmRepository.closeDeal(d.id, "won") }, "Negócio ganho 🎉")
                                }
                                ActionPill("Perder", Icons.Outlined.Cancel, c.border, c.muted, Modifier.weight(1f), enabled = !busy) {
                                    scope.launch { AppContainer.crmRepository.getLossReasons().onSuccess { lossReasons = it }; showLoss = true }
                                }
                                ActionPill("Próx. etapa", Icons.AutoMirrored.Filled.ArrowForward, c.border, c.text, Modifier.weight(1f), enabled = !busy && next != null) {
                                    next?.let { act({ AppContainer.crmRepository.moveDealStage(d.id, it.id) }, "Movido para \"${it.name}\"") }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        VdTabs(listOf("Timeline", "Dados", "Produtos", "Docs"), tab, { tab = it })
                    }
                }
            )

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                d == null -> VdEmptyState(Icons.Outlined.ViewKanban, "Negócio não encontrado", error ?: "")
                else -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (tab) {
                        "Timeline" -> {
                            DealTimelineExtras(extras)
                            TimelineItem("Estágio atual: ${d.stage?.name ?: "—"}", d.updatedAt?.let { relativeTime(it) } ?: "", "${d.owner?.name ?: "—"}${d.source?.let { " · origem ${it}" } ?: ""}", c.primarySurface, c.primary)
                            d.lastActivityAt?.let { TimelineItem("Última atividade", relativeTime(it), "Registrada no CRM", Tint.blueBg, Tint.blueFg) }
                            d.createdAt?.let { TimelineItem("Negócio criado", relativeTime(it), "Pipeline ${d.pipeline?.name ?: ""}", c.surfaceAlt, c.textTertiary, last = true) }
                        }
                        "Dados" -> VdCard(padding = 0.dp) {
                            DataRow("Contato", d.contact?.name ?: "—", accent = d.contact != null) { d.contact?.id?.let(onOpenContact) }
                            DataRow("Empresa", d.client?.name ?: "—")
                            DataRow("Responsável", d.owner?.name ?: "—")
                            DataRow("Pipeline", d.pipeline?.name ?: "—")
                            DataRow("Etapa", d.stage?.name ?: "—")
                            DataRow("Origem", d.source?.let { sourceLabel(it) } ?: "—")
                            DataRow("Previsão de fechamento", d.expectedCloseDate ?: "—")
                            DataRow("Criado", d.createdAt?.let { relativeTime(it) } ?: "—", last = d.description.isNullOrBlank())
                            if (!d.description.isNullOrBlank()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("DESCRIÇÃO", fontSize = 10.sp, color = c.muted)
                                    Text(d.description, fontSize = 13.sp, color = c.text, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                        "Produtos" -> DealProductsTab(extras)
                        else -> DealDocsTab(extras)
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Barra de ações fixa
            if (d != null) {
                Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
                    VdDivider()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BottomAction(Icons.Outlined.EditNote, "Nota", Modifier.weight(1f)) { activityPrompt = "note" }
                        BottomAction(Icons.Outlined.Phone, "Ligar", Modifier.weight(1f)) { dial(context, d.contact?.phoneNumber) { toast = it } }
                        BottomAction(Icons.Outlined.Forum, "Conversa", Modifier.weight(1f)) {
                            val cid = d.contact?.id
                            if (cid == null) toast = "Negócio sem contato" else scope.launch {
                                AppContainer.crmRepository.getContactConversationId(cid).onSuccess { convId ->
                                    if (convId != null) onOpenConversation(convId) else toast = "Contato ainda não possui conversa"
                                }
                            }
                        }
                        BottomAction(Icons.Outlined.Email, "E-mail", Modifier.weight(1f)) {
                            val email = d.contact?.email
                            if (email.isNullOrBlank()) toast = "Contato sem e-mail"
                            else try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))) } catch (_: Exception) { toast = "Nenhum app de e-mail" }
                        }
                        BottomAction(Icons.Outlined.CheckBox, "Tarefa", Modifier.weight(1f)) { activityPrompt = "task" }
                    }
                }
            }
        }
        toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)) { VdToast(it) } }
    }

    if (showLoss && d != null) {
        ModalBottomSheet(
            onDismissRequest = { showLoss = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                Text("Motivo da perda", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                if (lossReasons.isEmpty()) Text("Nenhum motivo cadastrado — configure na versão web.", fontSize = 12.5.sp, color = c.muted, modifier = Modifier.padding(vertical = 8.dp))
                lossReasons.forEach { r ->
                    Text(
                        r.name, fontSize = 14.sp, color = c.text,
                        modifier = Modifier.fillMaxWidth().clickable {
                            showLoss = false
                            act({ AppContainer.crmRepository.closeDeal(d.id, "lost", r.id) }, "Negócio marcado como perdido")
                        }.padding(vertical = 12.dp)
                    )
                    VdDivider()
                }
            }
        }
    }
}

@Composable
private fun ActionPill(label: String, icon: ImageVector, border: Color, fg: Color, modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier.height(36.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, if (enabled) border else Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (enabled) fg else Color(0xFF9CA3AF), modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) fg else Color(0xFF9CA3AF))
    }
}

@Composable
private fun BottomAction(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier.height(38.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = c.textTertiary, modifier = Modifier.size(14.dp))
        Text(label, fontSize = 9.sp, color = c.text, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DataRow(label: String, value: String, accent: Boolean = false, last: Boolean = false, onClick: (() -> Unit)? = null) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = c.muted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (accent) c.primary else c.text)
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}

@Composable
private fun TimelineItem(title: String, time: String, subtitle: String, bg: Color, fg: Color, last: Boolean = false) {
    val c = AppTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(26.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ViewKanban, null, tint = fg, modifier = Modifier.size(14.dp))
            }
            if (!last) Box(Modifier.size(1.dp, 26.dp).background(c.divider))
        }
        Column(Modifier.weight(1f).padding(bottom = 10.dp)) {
            Row { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f)); Text(time, fontSize = 11.sp, color = c.placeholder) }
            Text(subtitle, fontSize = 12.sp, color = c.textTertiary)
        }
    }
}

private fun dial(context: android.content.Context, phone: String?, onError: (String) -> Unit) {
    if (phone.isNullOrBlank()) { onError("Contato sem telefone"); return }
    try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) } catch (_: Exception) { onError("Não foi possível ligar") }
}
