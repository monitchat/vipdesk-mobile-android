package br.com.vipdesk.mobile.ui.ticket

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.data.model.TimelineEvent
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    onBack: () -> Unit,
    onOpenContact: (Int) -> Unit,
    viewModel: TicketDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val t = uiState.ticket
    var showResolveConfirm by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            toast = it
            viewModel.clearMessage()
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    if (showResolveConfirm) {
        AlertDialog(
            onDismissRequest = { showResolveConfirm = false },
            containerColor = c.surface,
            icon = { Icon(Icons.Default.CheckCircle, null, tint = VdSuccess) },
            title = { Text("Resolver ticket", color = c.textPrimary) },
            text = { Text("Deseja marcar este ticket como resolvido?", color = c.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showResolveConfirm = false
                    viewModel.resolve()
                }) { Text("Resolver", color = VdSuccess, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResolveConfirm = false }) {
                    Text("Cancelar", color = c.textSecondary)
                }
            }
        )
    }

    if (uiState.showTransferDialog) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissTransferDialog() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Transferir para…",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (uiState.agents.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = c.accent, modifier = Modifier.size(22.dp))
                    }
                }
                uiState.agents.forEach { agent ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.transferTo(agent.id) }
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        VdAvatar(name = agent.name, size = 34.dp, fontSize = 12)
                        Column {
                            Text(
                                agent.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary
                            )
                            Text(agent.email, fontSize = 11.5.sp, color = c.textSecondary)
                        }
                    }
                    VdDivider()
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                }
                Text(
                    "Ticket ${t?.ticketNumber ?: ""}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (uiState.isActing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = c.accent,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                }
                t?.let {
                    val (stLabel, stColor) = ticketStatusVisual(it.status, it.isOpen)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(stColor.copy(alpha = 0.15f))
                            .clickable { showActions = true }
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(stLabel, fontSize = 12.sp, color = stColor)
                    }
                }
            }

            when {
                uiState.isLoading && t == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = c.accent)
                    }
                }
                t == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            uiState.error ?: "Ticket não encontrado",
                            color = c.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            t.title ?: "Sem título",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp,
                            color = c.textPrimary
                        )

                        // Alerta de SLA
                        t.sla?.let { sla ->
                            if (!sla.within) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(VdDanger.copy(alpha = 0.10f))
                                        .border(1.dp, VdDanger.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Timer, null,
                                        tint = VdDanger, modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        sla.label.ifBlank { "SLA em risco" },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = VdDanger
                                    )
                                }
                            }
                        }

                        // Campos
                        VdCard(padding = 0.dp) {
                            Column(Modifier.padding(horizontal = 14.dp)) {
                                val (priLabel, priColor) = ticketPriorityVisual(t.priority)
                                FieldRow("Cliente", t.contact?.name ?: "—", c.textPrimary) {
                                    t.contact?.id?.let(onOpenContact)
                                }
                                FieldRow("Agente", t.responsible?.name ?: "Não atribuído", c.textPrimary)
                                FieldRow("Departamento", t.department ?: "—", c.textPrimary)
                                FieldRow("Prioridade", priLabel, priColor)
                                FieldRow("Canal", sourceLabel(t.channel), c.textPrimary)
                                FieldRow(
                                    "Criado",
                                    t.createdAt?.let { relativeTime(it) } ?: "—",
                                    c.textPrimary,
                                    last = true
                                )
                            }
                        }

                        // Atividade
                        if (t.timeline.isNotEmpty()) {
                            VdCard {
                                VdSectionLabel("Atividade", Modifier.padding(bottom = 4.dp))
                                t.timeline.forEachIndexed { i, event ->
                                    TimelineRow(event, showDivider = i < t.timeline.lastIndex)
                                }
                            }
                        }

                        // Ações
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            if (t.isOpen) {
                                VdOutlineButton(
                                    label = "Resolver",
                                    icon = Icons.Outlined.CheckCircle,
                                    color = VdSuccess,
                                    background = VdSuccess.copy(alpha = 0.12f),
                                    enabled = !uiState.isActing,
                                    onClick = { showResolveConfirm = true },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            VdOutlineButton(
                                label = "Atribuir a mim",
                                icon = Icons.Outlined.PersonAddAlt,
                                enabled = !uiState.isActing,
                                onClick = { viewModel.assignToMe() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        VdOutlineButton(
                            label = "Transferir para outro agente",
                            icon = Icons.Outlined.SwapHoriz,
                            color = c.textSecondary,
                            enabled = !uiState.isActing,
                            onClick = { viewModel.openTransferDialog() },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(40.dp))
                    }
                }
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

    if (showActions && t != null) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Ações do ticket",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                if (t.isOpen) {
                    VdSheetRow(
                        Icons.Outlined.CheckCircle, "Resolver ticket",
                        iconTint = VdSuccess, textColor = VdSuccess,
                        onClick = {
                            showActions = false
                            showResolveConfirm = true
                        }
                    )
                }
                VdSheetRow(Icons.Outlined.PersonAddAlt, "Atribuir a mim", onClick = {
                    showActions = false
                    viewModel.assignToMe()
                })
                VdSheetRow(Icons.Outlined.SwapHoriz, "Transferir para outro agente", onClick = {
                    showActions = false
                    viewModel.openTransferDialog()
                })
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    valueColor: Color,
    last: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    var m = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp)
    if (onClick != null) m = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 10.dp)
    Row(modifier = m, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            fontSize = 12.sp,
            color = c.textSecondary,
            modifier = Modifier.width(110.dp)
        )
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
    if (!last) VdDivider()
}

@Composable
private fun TimelineRow(event: TimelineEvent, showDivider: Boolean) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(c.chip, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CheckCircle, null,
                tint = c.textSecondary, modifier = Modifier.size(13.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(event.message, fontSize = 13.sp, lineHeight = 18.sp, color = c.textPrimary)
            Text(
                buildString {
                    event.user?.let { append("$it · ") }
                    event.createdAt?.let { append(relativeTime(it)) }
                },
                fontSize = 10.5.sp,
                color = c.textSecondary,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
    if (showDivider) VdDivider()
}
