package br.com.vipdesk.mobile.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.data.model.TimelineEvent
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.components.SectionCard
import br.com.vipdesk.mobile.ui.components.StatusBadge
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    onBack: () -> Unit,
    onOpenContact: (Int) -> Unit,
    viewModel: TicketDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val t = uiState.ticket
    var showResolveConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showResolveConfirm) {
        AlertDialog(
            onDismissRequest = { showResolveConfirm = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = VipDeskGreen) },
            title = { Text("Resolver ticket") },
            text = { Text("Deseja marcar este ticket como resolvido?") },
            confirmButton = {
                TextButton(onClick = {
                    showResolveConfirm = false
                    viewModel.resolve()
                }) { Text("Resolver", color = VipDeskGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResolveConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    if (uiState.showTransferDialog) {
        AgentPickerDialog(
            agents = uiState.agents,
            onPick = { viewModel.transferTo(it.id) },
            onDismiss = { viewModel.dismissTransferDialog() }
        )
    }

    Scaffold(
        containerColor = c.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Ticket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    if (uiState.isActing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(end = 8.dp),
                            color = VipDeskPurple,
                            strokeWidth = 2.dp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.textPrimary,
                    navigationIconContentColor = c.textPrimary,
                    actionIconContentColor = c.textPrimary
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading && t == null -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = VipDeskPurple)
                }
            }
            t == null -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text(uiState.error ?: "Ticket não encontrado", color = c.textSecondary)
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Ticket card
                    val (statusLabel, statusColor) = ticketStatusVisual(t.status, t.isOpen)
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ticket", fontSize = 12.sp, color = c.textSecondary)
                                    Text(
                                        text = "#${t.ticketNumber ?: t.id}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = c.textPrimary
                                    )
                                }
                                StatusBadge(label = statusLabel, color = statusColor)
                            }
                            t.createdAt?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Criado ${relativeTime(it)}",
                                    fontSize = 12.sp,
                                    color = c.textSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = c.divider)
                            Spacer(modifier = Modifier.height(6.dp))

                            val (prioLabel, prioColor) = ticketPriorityVisual(t.priority)
                            DetailRow(Icons.Default.Person, "Responsável") {
                                Text(
                                    t.responsible?.name ?: "Não atribuído",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.textPrimary
                                )
                            }
                            DetailRow(Icons.Default.Flag, "Prioridade") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(prioColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        prioLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = c.textPrimary
                                    )
                                }
                            }
                            DetailRow(Icons.Default.Speed, "SLA") {
                                Text(
                                    t.sla?.label ?: "—",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (t.sla?.within != false) VipDeskGreen else VipDeskRed
                                )
                            }
                            DetailRow(Icons.Default.Forum, "Canal") {
                                val v = sourceVisual(t.channel)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(v.icon, null, tint = v.color, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        t.channel?.replaceFirstChar { it.uppercase() } ?: "—",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = c.textPrimary
                                    )
                                }
                            }
                            t.contact?.let { contact ->
                                DetailRow(Icons.Default.Person, "Contato") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            contact.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VipDeskPurple
                                        )
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            null,
                                            tint = c.iconMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // History / timeline
                    Text(
                        "Histórico",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (t.timeline.isEmpty()) {
                                Text(
                                    "Sem eventos registrados",
                                    fontSize = 13.sp,
                                    color = c.textSecondary
                                )
                            } else {
                                t.timeline.forEachIndexed { i, ev ->
                                    TimelineRow(ev, isLast = i == t.timeline.lastIndex)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.assignToMe() },
                            enabled = !uiState.isActing,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VipDeskPurple)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Atribuir", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.openTransferDialog() },
                            enabled = !uiState.isActing,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VipDeskPurple)
                        ) {
                            Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transferir", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showResolveConfirm = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipDeskGreen),
                        enabled = t.isOpen && !uiState.isActing
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (t.isOpen) "Resolver ticket" else "Ticket resolvido",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    trailing: @Composable () -> Unit
) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = c.iconMuted, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 13.sp, color = c.textSecondary, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun TimelineRow(ev: TimelineEvent, isLast: Boolean) {
    val c = AppTheme.colors
    val color = when (ev.type) {
        "created" -> VipDeskBlue
        "first_response" -> VipDeskGreen
        "closed" -> VipDeskGreen
        else -> VipDeskPurple
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(c.divider)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(
                text = ev.message,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                ev.user?.let {
                    Text("$it · ", fontSize = 11.sp, color = c.textSecondary)
                }
                ev.createdAt?.let {
                    Text(relativeTime(it), fontSize = 11.sp, color = c.textSecondary)
                }
            }
        }
    }
}

@Composable
private fun AgentPickerDialog(
    agents: List<br.com.vipdesk.mobile.data.model.User>,
    onPick: (br.com.vipdesk.mobile.data.model.User) -> Unit,
    onDismiss: () -> Unit
) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) agents
    else agents.filter {
        it.name.contains(query, true) || it.email.contains(query, true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transferir para", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar atendente...", fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (agents.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                        CircularProgressIndicator(color = VipDeskPurple, strokeWidth = 2.dp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered, key = { it.id }) { agent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPick(agent) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PurpleGradientLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        initialsOf(agent.name),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        agent.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = c.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        agent.email,
                                        fontSize = 12.sp,
                                        color = c.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(color = c.divider)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
