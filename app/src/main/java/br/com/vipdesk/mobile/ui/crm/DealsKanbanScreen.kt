package br.com.vipdesk.mobile.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.rememberDragBoardState
import br.com.vipdesk.mobile.ui.components.dropHighlight
import br.com.vipdesk.mobile.ui.components.dragColumn
import br.com.vipdesk.mobile.ui.components.dragCard
import br.com.vipdesk.mobile.ui.components.DragOverlay
import br.com.vipdesk.mobile.ui.components.DragBoardState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.DealPipeline
import br.com.vipdesk.mobile.data.model.DealStage
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint

private val STAGE_COLORS = listOf(Color(0xFF3B82F6), Color(0xFF7E3E97), Color(0xFFF59E0B), Color(0xFF0891B2), Color(0xFF22C55E), Color(0xFFEF4444))

/** Kanban de negócios (tela 17): colunas 290px por etapa, scroll horizontal, indicador no rodapé. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsKanbanScreen(
    onBack: () -> Unit,
    onDealClick: (Int) -> Unit,
    onCreateDeal: () -> Unit
) {
    val c = AppTheme.colors
    var pipelines by remember { mutableStateOf<List<DealPipeline>>(emptyList()) }
    var pipeline by remember { mutableStateOf<DealPipeline?>(null) }
    var deals by remember { mutableStateOf<List<ApiDeal>>(emptyList()) }
    val drag = rememberDragBoardState()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showPipelines by remember { mutableStateOf(false) }
    var onlyMine by remember { mutableStateOf(false) }
    var myId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toast) { if (toast != null) { kotlinx.coroutines.delay(2200); toast = null } }

    LaunchedEffect(Unit) {
        myId = AppContainer.tokenManager.getUserId()
        AppContainer.crmRepository.getPipelines().fold(
            onSuccess = { list -> pipelines = list; pipeline = list.find { it.isDefault } ?: list.firstOrNull() },
            onFailure = { error = it.message; loading = false }
        )
    }
    LaunchedEffect(pipeline?.id, CrmEvents.dealsVersion) {
        val p = pipeline ?: return@LaunchedEffect
        loading = true
        AppContainer.crmRepository.listDeals(null, null, pipelineId = p.id, kanban = true).fold(
            onSuccess = { deals = it; error = null },
            onFailure = { error = it.message }
        )
        loading = false
    }

    val visible = if (onlyMine && myId != null) deals.filter { it.owner?.id == myId } else deals
    val stages = pipeline?.stages.orEmpty()
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(
            title = "Negócios",
            subtitle = pipeline?.let { "Pipeline: ${it.name}" },
            onBack = onBack,
            actions = { VdHeaderIcon(Icons.Outlined.AddCircleOutline, "Novo negócio", onCreateDeal, tint = c.primary) },
            below = {
                Row(
                    Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp))
                        .clickable { showPipelines = true }.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pipeline: ${pipeline?.name ?: "—"}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                    Icon(Icons.Outlined.KeyboardArrowDown, null, tint = c.placeholder, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VdPill("Vendedor: eu", onlyMine, { onlyMine = !onlyMine }, soft = true)
                    VdPill("Todos", !onlyMine, { onlyMine = false }, soft = true)
                }
            }
        )

        when {
            loading && deals.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            error != null -> VdEmptyState(Icons.Outlined.ViewKanban, "Não foi possível carregar", error ?: "")
            stages.isEmpty() -> VdEmptyState(Icons.Outlined.ViewKanban, "Nenhum pipeline", "Configure um funil na versão web.")
            else -> {
                Row(
                    Modifier.weight(1f).fillMaxWidth().horizontalScroll(scroll).padding(start = 12.dp, end = 12.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stages.forEachIndexed { i, stage ->
                        val col = visible.filter { it.stage?.id == stage.id || it.stageId == stage.id }
                        StageColumn(stage, col, STAGE_COLORS[i % STAGE_COLORS.size], onDealClick, onCreateDeal, drag) { dealId, targetStageId ->
                            scope.launch {
                                AppContainer.crmRepository.moveDealStage(dealId.toInt(), targetStageId.toInt()).fold(
                                    { toast = "Negócio movido"; CrmEvents.dealsVersion++ },
                                    { toast = it.message ?: "Não foi possível mover" }
                                )
                            }
                        }
                    }
                }
                // Indicador de colunas
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val page = if (stages.isEmpty()) 0 else (scroll.value / 300).coerceIn(0, stages.lastIndex)
                    stages.forEachIndexed { i, s ->
                        val color = when { s.isWon -> c.success; s.isLost -> c.danger; else -> c.border }
                        Box(Modifier.width(if (i == page) 18.dp else 5.dp).height(5.dp).background(if (i == page) c.primary else color, RoundedCornerShape(3.dp)))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(stages.joinToString(" · ") { it.name.take(8) }, fontSize = 10.sp, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    DragOverlay(drag, widthDp = 270) {
        deals.firstOrNull { it.id.toString() == drag.draggingId }?.let { d -> DealCard(d) {} }
    }
    toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)) { VdToast(it) } }
    }

    if (showPipelines) {
        ModalBottomSheet(
            onDismissRequest = { showPipelines = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
                Text("Pipeline", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                pipelines.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth().clickable { pipeline = p; showPipelines = false }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.name, fontSize = 14.sp, color = c.text, modifier = Modifier.weight(1f))
                        if (p.id == pipeline?.id) Text("✓", color = c.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StageColumn(
    stage: DealStage, deals: List<ApiDeal>, color: Color, onDealClick: (Int) -> Unit, onCreateDeal: () -> Unit,
    drag: DragBoardState, onDrop: (String, String) -> Unit
) {
    val c = AppTheme.colors
    val sum = deals.sumOf { it.value ?: 0.0 }
    Column(
        Modifier.width(290.dp).fillMaxSize()
            .dragColumn(drag, stage.id.toString())
            .dropHighlight(drag, stage.id.toString(), c.primary),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stage.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(" · ${deals.size}", fontSize = 13.sp, color = c.muted, modifier = Modifier.weight(1f))
            Text(formatDealValue(sum, "BRL").takeIf { sum > 0 } ?: "R$ 0", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.muted)
        }
        Box(Modifier.fillMaxWidth().height(3.dp).background(if (stage.isWon) c.success else if (stage.isLost) c.danger else color, RoundedCornerShape(2.dp)))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            deals.forEach { deal ->
                DealCard(deal, Modifier.dragCard(drag, deal.id.toString(), stage.id.toString(), onDrop = onDrop)) { onDealClick(deal.id) }
            }
            if (!stage.isWon && !stage.isLost) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, c.border, RoundedCornerShape(10.dp))
                        .clickable(onClick = onCreateDeal).padding(10.dp),
                    contentAlignment = Alignment.Center
                ) { Text("+ Novo negócio", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = c.primary) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DealCard(deal: ApiDeal, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(deal.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            listOfNotNull(deal.client?.name, deal.contact?.name).joinToString(" · ").ifBlank { "Sem contato" },
            fontSize = 12.sp, color = c.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatDealValue(deal.value, deal.currency), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
            deal.owner?.name?.let { VdAvatar(name = it, size = 20.dp, fontSize = 8, agent = true) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val stale = deal.lastActivityAt?.let { relativeTime(it) }
            if (stale != null) Text("atividade $stale", fontSize = 10.sp, color = c.muted)
            deal.expectedCloseDate?.let {
                Text("fecha $it", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Tint.greenFg,
                    modifier = Modifier.background(Tint.greenBg, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
