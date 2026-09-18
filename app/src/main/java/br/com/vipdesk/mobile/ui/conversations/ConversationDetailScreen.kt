package br.com.vipdesk.mobile.ui.conversations

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.BuildConfig
import br.com.vipdesk.mobile.data.model.FastMessage
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.data.model.Comment
import br.com.vipdesk.mobile.data.model.Message
import br.com.vipdesk.mobile.data.model.Ticket
import br.com.vipdesk.mobile.data.model.User
import br.com.vipdesk.mobile.data.notifications.ActiveConversation
import br.com.vipdesk.mobile.data.notifications.MessageNotifier
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.AudioPlayer
import br.com.vipdesk.mobile.ui.components.DocumentMessage
import br.com.vipdesk.mobile.ui.components.InlineImage
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdInput
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VdTabs
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.VideoThumbnail
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusTint
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Inter
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Entrada unificada da linha do tempo: mensagem ou comentário interno. */
private sealed class ThreadItem(val sortDate: java.util.Date?) {
    class Msg(val message: Message) : ThreadItem(parseMsgDate(message.createdAt))
    class Note(val comment: Comment) : ThreadItem(parseMsgDate(comment.createdAt))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    onBack: () -> Unit,
    viewModel: ConversationDetailViewModel,
    onOpenTicket: (Int) -> Unit = {},
    onOpenContact: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var internalMode by remember { mutableStateOf(false) }
    var showQuickReplies by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showContact by remember { mutableStateOf(false) }
    var showNewTicket by remember { mutableStateOf(false) }
    var showSchedule by remember { mutableStateOf(false) }
    var pinned by remember { mutableStateOf(false) }
    var confirmResolve by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.uploadFile(it) } }

    LaunchedEffect(Unit) { viewModel.loadComments() }

    DisposableEffect(viewModel.conversationId) {
        ActiveConversation.id = viewModel.conversationId
        MessageNotifier.cancel(context, viewModel.conversationId)
        onDispose {
            if (ActiveConversation.id == viewModel.conversationId) ActiveConversation.id = null
        }
    }

    // Data ausente vai para o fim (mensagem recém-enviada); sortedBy é estável.
    val thread = remember(uiState.messages, uiState.comments) {
        (uiState.messages.map { ThreadItem.Msg(it) } + uiState.comments.map { ThreadItem.Note(it) })
            .sortedBy { it.sortDate?.time ?: Long.MAX_VALUE }
    }

    LaunchedEffect(thread.size) {
        if (thread.isNotEmpty()) try { listState.scrollToItem(thread.size - 1) } catch (_: Exception) {}
    }
    LaunchedEffect(uiState.error) { uiState.error?.let { toast = it; viewModel.clearError() } }
    LaunchedEffect(uiState.actionSuccess) { uiState.actionSuccess?.let { toast = it; viewModel.clearActionSuccess() } }
    LaunchedEffect(toast) { if (toast != null) { delay(2200); toast = null } }

    if (uiState.showTransferDialog) {
        UserSelectionDialog("Transferir atendimento", uiState.users, { viewModel.transferToUser(it.id) }) { viewModel.hideTransferDialog() }
    }
    if (uiState.showAssignDialog) {
        UserSelectionDialog("Atribuir atendente", uiState.users, { viewModel.assignToUser(it.id) }) { viewModel.hideAssignDialog() }
    }
    if (confirmResolve) {
        AlertDialog(
            onDismissRequest = { confirmResolve = false },
            containerColor = c.surface,
            title = { Text("Finalizar ticket", color = c.text, fontWeight = FontWeight.SemiBold) },
            text = { Text("O ticket ativo desta conversa será marcado como resolvido.", color = c.muted) },
            confirmButton = {
                TextButton(onClick = { confirmResolve = false; viewModel.resolveActiveTicket() }) {
                    Text("Finalizar", color = Tint.greenFg, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmResolve = false }) { Text("Cancelar", color = c.muted) } }
        )
    }

    val conv = uiState.conversation
    val scope = rememberCoroutineScope()
    if (showSchedule) br.com.vipdesk.mobile.ui.agenda.AppointmentCreateSheet(
        onDismiss = { showSchedule = false }, onCreated = { toast = it },
        contactId = conv?.contact?.id, contactName = conv?.contact?.name, phoneNumber = conv?.contact?.phoneNumber
    )
    val contact = conv?.contact
    val ticket = conv?.activeTicket
    val source = conv?.source

    Box(Modifier.fillMaxSize().background(c.chatBackground)) {
        Column(Modifier.fillMaxSize()) {
            // ————— Header (56px) —————
            Column(Modifier.fillMaxWidth().background(c.surface).statusBarsPadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 2.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textTertiary, modifier = Modifier.size(22.dp))
                    }
                    Box(Modifier.clickable { showContact = true }) {
                        VdAvatar(name = contact?.name ?: "?", size = 36.dp, fontSize = 12, source = source)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f).clickable { showContact = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                contact?.name ?: "Carregando…",
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(Icons.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(14.dp))
                        }
                        Text(
                            listOfNotNull(
                                contact?.phoneNumber,
                                contact?.client?.name,
                                ticket?.departmentName ?: sourceLabel(source)
                            ).joinToString(" · "),
                            fontSize = 11.sp, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (uiState.isSending) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = c.primary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    IconButton(onClick = { dial(context, contact?.phoneNumber) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Outlined.Phone, "Ligar", tint = c.textTertiary, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { showActions = true }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.MoreVert, "Ações", tint = c.textTertiary, modifier = Modifier.size(22.dp))
                    }
                }
                if (ticket != null) {
                    val (stLabel, _) = ticketStatusVisual(ticket.statusName ?: ticket.status, null)
                    val (bg, fg) = ticketStatusTint(stLabel)
                    Row(
                        Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VdTag("Ticket #${ticket.id} · $stLabel", color = fg, background = bg)
                        ticket.user?.name?.let { VdTag(it, color = c.primary, background = c.primarySurface) }
                        Spacer(Modifier.weight(1f))
                        if (conv.autoReply == 1) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(Icons.Outlined.SmartToy, null, tint = c.info, modifier = Modifier.size(12.dp))
                                Text("bot ativo", fontSize = 10.sp, color = c.muted)
                            }
                        }
                    }
                }
                VdDivider()
            }

            // ————— Linha do tempo —————
            Box(Modifier.weight(1f)) {
                if (uiState.isLoadingMessages && thread.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        itemsIndexed(
                            items = thread,
                            key = { idx, item ->
                                when (item) {
                                    is ThreadItem.Msg -> "m${item.message.id}_$idx"
                                    is ThreadItem.Note -> "n${item.comment.id ?: idx}_$idx"
                                }
                            }
                        ) { idx, item ->
                            val prevDate = if (idx > 0) thread[idx - 1].sortDate else null
                            if (dayKey(item.sortDate) != dayKey(prevDate)) DaySeparator(dayLabel(item.sortDate))
                            when (item) {
                                is ThreadItem.Msg -> MessageBubble(item.message, isOwnMessage = item.message.sender == 1)
                                is ThreadItem.Note -> NoteBubble(item.comment)
                            }
                        }
                    }
                }
            }

            // ————— Composer —————
            Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding().imePadding()) {
                VdDivider()
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 10.dp, end = 10.dp, top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModeChip("Responder ao cliente", selected = !internalMode, internal = false) { internalMode = false }
                    ModeChip("Comentário interno", selected = internalMode, internal = true) { internalMode = true }
                    Spacer(Modifier.weight(1f))
                    Row(
                        Modifier.clickable { showQuickReplies = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Outlined.Description, null, tint = c.muted, modifier = Modifier.size(14.dp))
                        Text("Templates", fontSize = 11.sp, color = c.muted)
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 6.dp, end = 10.dp, top = 8.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.AddCircleOutline, "Anexar", tint = c.muted, modifier = Modifier.size(26.dp))
                    }
                    val text = if (internalMode) uiState.commentText else uiState.messageText
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (internalMode) c.internalComposer else c.surface)
                            .border(1.dp, if (internalMode) c.warning else c.border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                if (internalMode) "Comentário interno (não vai ao cliente)…" else "Mensagem…",
                                fontSize = 13.sp, color = c.placeholder
                            )
                        }
                        BasicTextField(
                            value = text,
                            onValueChange = { if (internalMode) viewModel.onCommentTextChange(it) else viewModel.onMessageTextChange(it) },
                            textStyle = TextStyle(fontSize = 13.sp, color = c.text, fontFamily = Inter),
                            cursorBrush = SolidColor(c.primary),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    val canSend = text.isNotBlank() && !uiState.isSending
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (canSend) c.primary else c.border)
                            .clickable(enabled = canSend) { if (internalMode) viewModel.sendComment() else viewModel.sendMessage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("/ msgs rápidas", fontSize = 10.sp, color = c.muted, modifier = Modifier.clickable { showQuickReplies = true })
                    Text("@ mencionar", fontSize = 10.sp, color = c.muted)
                    Text("🔒 interno = só a equipe vê", fontSize = 10.sp, color = c.muted)
                }
            }
        }

        toast?.let {
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 140.dp)) { VdToast(it) }
        }
    }

    // ————— Respostas rápidas —————
    if (showQuickReplies) {
        ModalBottomSheet(
            onDismissRequest = { showQuickReplies = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            var fastMessages by remember { mutableStateOf<List<FastMessage>?>(null) }
            var fastError by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                runCatching { AppContainer.apiService.getFastMessages() }.fold(
                    onSuccess = { r -> if (r.isSuccessful) fastMessages = r.body()?.data.orEmpty() else fastError = "Erro ${r.code()}" },
                    onFailure = { fastError = it.message ?: "Sem conexão" }
                )
            }
            var quickQuery by remember { mutableStateOf("") }
            val shown = fastMessages?.filter {
                quickQuery.isBlank() ||
                    it.title.orEmpty().contains(quickQuery, true) || it.message.orEmpty().contains(quickQuery, true)
            }
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp).heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                Text("Mensagens rápidas" + (fastMessages?.size?.takeIf { it > 0 }?.let { " · $it" } ?: ""), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
                // Empresas com dezenas de mensagens: busca evita rolar a lista inteira
                if ((fastMessages?.size ?: 0) > 8) {
                    br.com.vipdesk.mobile.ui.components.VdSearchField(quickQuery, { quickQuery = it }, "Buscar mensagem")
                    Spacer(Modifier.height(8.dp))
                }
                when {
                    fastError != null -> Text(fastError ?: "", fontSize = 13.sp, color = c.danger, modifier = Modifier.padding(vertical = 12.dp))
                    fastMessages == null -> Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
                    fastMessages!!.isEmpty() -> Text("Nenhuma mensagem rápida cadastrada. Crie em Configurações › Respostas rápidas.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(vertical = 12.dp))
                    shown!!.isEmpty() -> Text("Nenhuma mensagem encontrada para \"$quickQuery\".", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(vertical = 12.dp))
                    else -> shown.forEach { reply ->
                        Column(
                            Modifier.fillMaxWidth().clickable {
                                viewModel.onMessageTextChange(reply.message.orEmpty()); internalMode = false; showQuickReplies = false
                            }.padding(vertical = 10.dp)
                        ) {
                            Text(reply.title.orEmpty().ifBlank { "#${reply.id}" }, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                            Text(reply.message.orEmpty(), fontSize = 13.sp, color = c.textTertiary, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
                        }
                        HorizontalDivider(color = c.divider)
                    }
                }
            }
        }
    }

    // ————— Ações (tela 11) —————
    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 4.dp).padding(bottom = 24.dp)) {
                VdSheetRow(Icons.Outlined.PanTool, "Assumir conversa", onClick = { showActions = false; viewModel.assignToMe() })
                VdSheetRow(Icons.Outlined.SwapHoriz, "Transferir", trailing = "depto ou atendente ›", onClick = { showActions = false; viewModel.showTransferDialog() })
                VdSheetRow(Icons.Outlined.ConfirmationNumber, "Abrir ticket", onClick = { showActions = false; showNewTicket = true })
                VdSheetRow(Icons.Outlined.CalendarMonth, "Agendar", onClick = { showActions = false; showSchedule = true })
                val botOn = (conv?.autoReply ?: 0) != 0
                VdSheetRow(Icons.Outlined.SmartToy, if (botOn) "Pausar bot / auto-resposta" else "Reativar bot / auto-resposta", trailing = if (botOn) "bot ativo" else "bot pausado", onClick = {
                    showActions = false
                    scope.launch {
                        br.com.vipdesk.mobile.ui.modules.apiPost("chat/conversation/${conv?.id}/autoReply", br.com.vipdesk.mobile.ui.modules.json("conversation_id" to conv?.id, "auto_reply" to !botOn)).fold(
                            { toast = if (botOn) "Bot pausado nesta conversa" else "Bot reativado"; viewModel.loadAll() }, { toast = it.message }
                        )
                    }
                })
                VdSheetRow(Icons.Outlined.Phone, "Ligar", onClick = { showActions = false; dial(context, contact?.phoneNumber) })
                VdSheetRow(Icons.Outlined.PushPin, if (pinned) "Desafixar conversa" else "Fixar conversa", onClick = {
                    showActions = false
                    scope.launch {
                        br.com.vipdesk.mobile.ui.modules.apiPut("conversation/${conv?.id}/pin", br.com.vipdesk.mobile.ui.modules.json("pinned" to !pinned)).fold(
                            { pinned = !pinned; toast = if (pinned) "Conversa fixada no topo" else "Conversa desafixada" }, { toast = it.message }
                        )
                    }
                })
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                VdSheetRow(
                    Icons.Outlined.CheckCircle, "Finalizar ticket",
                    iconTint = Tint.greenFg, textColor = Tint.greenFg, bold = true,
                    trailing = if (ticket == null) "sem ticket ativo" else "motivo + resolução",
                    onClick = { showActions = false; if (ticket != null) confirmResolve = true else toast = "Esta conversa não tem ticket ativo" }
                )
            }
        }
    }

    // ————— Abrir ticket —————
    if (showNewTicket) {
        var title by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("normal") }
        ModalBottomSheet(
            onDismissRequest = { showNewTicket = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Abrir ticket", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                Text("Para ${contact?.name ?: "este contato"} · vinculado a esta conversa", fontSize = 12.sp, color = c.muted)
                VdInput("Assunto", title, { title = it }, "Resumo do problema")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Baixa" to "low", "Média" to "normal", "Alta" to "high").forEach { (l, k) ->
                        VdPill(l, priority == k, { priority = k })
                    }
                }
                VdButton("Criar ticket", enabled = title.isNotBlank(), onClick = {
                    showNewTicket = false
                    viewModel.createTicket(title.trim(), priority) { onOpenTicket(it) }
                }, modifier = Modifier.fillMaxWidth())
            }
        }
    }

    // ————— Painel do contato (sheet ~80%, 4 abas) —————
    if (showContact && conv != null) {
        var tab by remember { mutableStateOf("Detalhes") }
        ModalBottomSheet(
            onDismissRequest = { showContact = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            Column(Modifier.fillMaxWidth().fillMaxHeight(0.82f)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VdAvatar(name = conv.contact.name, size = 48.dp, fontSize = 15)
                    Column(Modifier.weight(1f)) {
                        Text(conv.contact.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                        Text(
                            listOfNotNull("Cliente".takeIf { conv.contact.client != null } ?: "Contato", conv.contact.client?.name).joinToString(" · "),
                            fontSize = 12.sp, color = c.muted
                        )
                    }
                    IconButton(onClick = { showContact = false; onOpenContact(conv.contact.id) }) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, "Contato 360", tint = c.primary, modifier = Modifier.size(20.dp))
                    }
                }
                VdTabs(listOf("Detalhes", "Ticket", "Histórico", "Timeline"), tab, { tab = it })
                VdDivider()
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (tab) {
                        "Detalhes" -> {
                            KeyValue("Telefone", conv.contact.phoneNumber ?: "—")
                            KeyValue("E-mail", conv.contact.email ?: "—")
                            KeyValue("Empresa / cliente", conv.contact.client?.name ?: "—", accent = conv.contact.client != null)
                            KeyValue("Canal", sourceLabel(source))
                            KeyValue("Conta", conv.accountNumber ?: "—")
                            Spacer(Modifier.height(8.dp))
                            VdButton("Contato 360", onClick = { showContact = false; onOpenContact(conv.contact.id) }, style = VdButtonStyle.Secondary, height = 40.dp, modifier = Modifier.fillMaxWidth())
                        }
                        "Ticket" -> {
                            if (ticket == null) {
                                Text("Sem ticket ativo nesta conversa.", fontSize = 13.sp, color = c.muted)
                                VdButton("Abrir ticket", onClick = { showContact = false; showNewTicket = true }, height = 40.dp, modifier = Modifier.fillMaxWidth())
                            } else {
                                val (stLabel, _) = ticketStatusVisual(ticket.statusName ?: ticket.status, null)
                                val (bg, fg) = ticketStatusTint(stLabel)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#${ticket.id} · ${ticket.title ?: "Sem título"}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                                    VdTag(stLabel, color = fg, background = bg)
                                }
                                val (priLabel, priColor) = ticketPriorityVisual(ticket.priority)
                                KeyValue("Prioridade", priLabel, dot = priColor)
                                KeyValue("Departamento", ticket.departmentName ?: "—")
                                KeyValue("Responsável", ticket.user?.name ?: "sem responsável")
                                KeyValue("Criado", ticket.createdAt?.let { relativeTime(it) } ?: "—")
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    VdButton("Transferir", onClick = { showContact = false; viewModel.showTransferDialog() }, style = VdButtonStyle.Secondary, height = 40.dp, modifier = Modifier.weight(1f))
                                    VdButton("Ver ticket", onClick = { showContact = false; onOpenTicket(ticket.id) }, style = VdButtonStyle.Secondary, height = 40.dp, modifier = Modifier.weight(1f))
                                }
                                VdButton("Finalizar ticket", onClick = { showContact = false; confirmResolve = true }, style = VdButtonStyle.Success, height = 40.dp, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        "Histórico" -> {
                            if (uiState.tickets.isEmpty()) Text("Nenhum ticket anterior.", fontSize = 13.sp, color = c.muted)
                            uiState.tickets.forEach { t -> TicketHistoryCard(t) { showContact = false; onOpenTicket(t.id) } }
                        }
                        "Timeline" -> {
                            TimelineRow("Conversa ${sourceLabel(source)}", conv.lastMessage?.createdAt ?: "", "Última mensagem", last = uiState.tickets.isEmpty())
                            uiState.tickets.forEachIndexed { i, t ->
                                TimelineRow("Ticket #${t.id} · ${t.title ?: ""}", t.createdAt?.let { relativeTime(it) } ?: "", t.statusName ?: t.status ?: "", last = i == uiState.tickets.lastIndex)
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

// ————— Peças —————

@Composable
private fun ModeChip(label: String, selected: Boolean, internal: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    val bg = when {
        selected && internal -> c.warning
        selected -> c.primary
        else -> c.surface
    }
    val fg = if (selected) Color.White else c.textTertiary
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, if (selected) bg else c.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (internal) Icon(Icons.Outlined.Lock, null, tint = fg, modifier = Modifier.size(12.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun KeyValue(label: String, value: String, accent: Boolean = false, dot: Color? = null) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 13.sp, color = c.muted, modifier = Modifier.weight(1f))
        if (dot != null) { Box(Modifier.size(8.dp).background(dot, CircleShape)); Spacer(Modifier.width(6.dp)) }
        Text(value, fontSize = 13.sp, fontWeight = if (accent) FontWeight.Medium else FontWeight.Normal, color = if (accent) c.primary else c.text)
    }
}

@Composable
private fun TicketHistoryCard(t: Ticket, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (stLabel, _) = ticketStatusVisual(t.statusName ?: t.status, null)
    val (bg, fg) = ticketStatusTint(stLabel)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, c.divider, RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#${t.id} · ${t.title ?: "Sem título"}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            VdTag(stLabel, color = fg, background = bg)
        }
        Text(listOfNotNull(t.createdAt?.let { relativeTime(it) }, t.departmentName, t.user?.name).joinToString(" · "), fontSize = 11.sp, color = c.muted)
    }
}

@Composable
private fun TimelineRow(title: String, time: String, subtitle: String, last: Boolean) {
    val c = AppTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(24.dp).background(c.primarySurface, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ConfirmationNumber, null, tint = c.primary, modifier = Modifier.size(13.dp))
            }
            if (!last) Box(Modifier.width(1.dp).height(28.dp).background(c.divider))
        }
        Column(Modifier.weight(1f).padding(bottom = 10.dp)) {
            Row { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis); Text(time, fontSize = 11.sp, color = c.placeholder) }
            Text(subtitle, fontSize = 12.sp, color = c.muted)
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isOwnMessage: Boolean) {
    val c = AppTheme.colors
    val cdnUrl = BuildConfig.CDN_URL
    val shape = if (isOwnMessage) RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp) else RoundedCornerShape(12.dp, 12.dp, 12.dp, 2.dp)
    val mediaUrl = message.getMediaUrl(cdnUrl)
    val msgType = message.messageType

    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start) {
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (isOwnMessage) c.agentBubble else c.surface)
                .border(1.dp, if (isOwnMessage) c.agentBubbleBorder else c.divider, shape)
        ) {
            Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                if (isOwnMessage && message.user != null && message.user.name.isNotBlank()) {
                    Text(message.user.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                    Spacer(Modifier.height(2.dp))
                }
                when {
                    msgType == 4 && mediaUrl != null -> InlineImage(imageUrl = mediaUrl)
                    (msgType == 5 || msgType == 2) && mediaUrl != null -> AudioPlayer(audioUrl = mediaUrl)
                    msgType == 3 && mediaUrl != null && message.fileName?.substringAfterLast(".", "")?.lowercase() in listOf("mp3", "wav", "ogg") -> AudioPlayer(audioUrl = mediaUrl)
                    msgType == 1 && mediaUrl != null -> VideoThumbnail(videoUrl = mediaUrl)
                    msgType == 3 && mediaUrl != null -> DocumentMessage(fileName = message.fileName, documentUrl = mediaUrl)
                }
                if (!message.message.isNullOrEmpty()) {
                    if (msgType in listOf(4, 1)) Spacer(Modifier.height(4.dp))
                    Text(message.message.replace(Regex("<[^>]*>"), ""), fontSize = 13.sp, lineHeight = 18.sp, color = c.text)
                }
            }
        }
        message.createdAt?.let { dateStr ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.padding(top = 2.dp)) {
                Text(formatMessageTime(dateStr), fontSize = 10.sp, color = c.muted)
                if (isOwnMessage) {
                    val read = (message.status ?: 0) >= 3
                    Icon(if (read) Icons.Default.DoneAll else Icons.Default.Done, null, tint = if (read) c.info else c.muted, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun NoteBubble(comment: Comment) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Box(
            Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                .background(c.internalBubble)
                .border(1.dp, c.internalBubbleBorder, RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                Text("Comentário interno · ${comment.user?.name ?: "Sistema"}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Tint.yellowFg)
                Text(comment.message ?: "", fontSize = 13.sp, lineHeight = 18.sp, color = c.text, modifier = Modifier.padding(top = 2.dp))
            }
        }
        comment.createdAt?.let { Text(formatMessageTime(it), fontSize = 10.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp)) }
    }
}

@Composable
private fun UserSelectionDialog(title: String, users: List<User>, onUserSelected: (User) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) users else users.filter {
        it.name.contains(searchQuery, true) || it.email.contains(searchQuery, true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(title, fontWeight = FontWeight.SemiBold, color = c.text) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery, onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar usuário…", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(filtered) { user ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onUserSelected(user) }.padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            VdAvatar(name = user.name, size = 36.dp, fontSize = 12, agent = true)
                            Column { Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.text); Text(user.email, fontSize = 12.sp, color = c.muted) }
                        }
                        HorizontalDivider(color = c.divider)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = c.muted) } }
    )
}

@Composable
private fun DaySeparator(label: String) {
    if (label.isBlank()) return
    val c = AppTheme.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            label, fontSize = 10.sp, color = c.muted,
            modifier = Modifier.padding(vertical = 4.dp).background(c.surface, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}

private fun dial(context: android.content.Context, phone: String?) {
    if (phone.isNullOrBlank()) return
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (_: Exception) {}
}

private fun formatMessageTime(dateStr: String): String {
    val date = parseMsgDate(dateStr) ?: return dateStr
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR")).format(date)
}

private fun parseMsgDate(dateStr: String?): java.util.Date? = br.com.vipdesk.mobile.ui.common.parseApiDate(dateStr)

private fun dayKey(date: java.util.Date?): String =
    if (date == null) "" else java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("pt", "BR")).format(date)

private fun dayLabel(date: java.util.Date?): String {
    if (date == null) return ""
    val cal = java.util.Calendar.getInstance()
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("pt", "BR"))
    val today = fmt.format(cal.time)
    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    val yesterday = fmt.format(cal.time)
    return when (fmt.format(date)) {
        today -> "Hoje"
        yesterday -> "Ontem"
        else -> java.text.SimpleDateFormat("dd 'de' MMMM", java.util.Locale("pt", "BR")).format(date)
    }
}
