package br.com.vipdesk.mobile.ui.conversations

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PersonAddAlt
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.BuildConfig
import br.com.vipdesk.mobile.data.demo.DEMO_QUICK_REPLIES
import br.com.vipdesk.mobile.data.model.Comment
import br.com.vipdesk.mobile.data.notifications.ActiveConversation
import br.com.vipdesk.mobile.data.notifications.MessageNotifier
import br.com.vipdesk.mobile.data.model.Message
import br.com.vipdesk.mobile.data.model.User
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.AudioPlayer
import br.com.vipdesk.mobile.ui.components.DocumentMessage
import br.com.vipdesk.mobile.ui.components.InlineImage
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VideoThumbnail
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdInfo
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.VdWarning
import kotlinx.coroutines.delay

/** Entrada unificada da linha do tempo: mensagem ou nota interna. */
private sealed class ThreadItem(val sortDate: java.util.Date?) {
    class Msg(val message: Message) : ThreadItem(parseMsgDate(message.createdAt))
    class Note(val comment: Comment) : ThreadItem(parseMsgDate(comment.createdAt))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    onBack: () -> Unit,
    viewModel: ConversationDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val listState = rememberLazyListState()
    var noteMode by remember { mutableStateOf(false) }
    var showQuickReplies by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadFile(it) }
    }

    // Notas internas entram na mesma linha do tempo (como no design)
    LaunchedEffect(Unit) { viewModel.loadComments() }

    // Conversa aberta não gera notificação do sistema; limpa as existentes.
    val context = LocalContext.current
    DisposableEffect(viewModel.conversationId) {
        ActiveConversation.id = viewModel.conversationId
        MessageNotifier.cancel(context, viewModel.conversationId)
        onDispose {
            if (ActiveConversation.id == viewModel.conversationId) {
                ActiveConversation.id = null
            }
        }
    }

    // Data ausente/não-parseável vai para o FIM (mensagem recém-enviada),
    // nunca para o topo — sortedBy é estável, então a ordem de chegada
    // se preserva entre itens de mesma chave.
    val thread = remember(uiState.messages, uiState.comments) {
        (uiState.messages.map { ThreadItem.Msg(it) } +
            uiState.comments.map { ThreadItem.Note(it) })
            .sortedBy { it.sortDate?.time ?: Long.MAX_VALUE }
    }

    LaunchedEffect(thread.size) {
        if (thread.isNotEmpty()) {
            try {
                listState.scrollToItem(thread.size - 1)
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            toast = it
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let {
            toast = it
            viewModel.clearActionSuccess()
        }
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2200)
            toast = null
        }
    }

    if (uiState.showTransferDialog) {
        UserSelectionDialog(
            title = "Transferir atendimento",
            users = uiState.users,
            onUserSelected = { viewModel.transferToUser(it.id) },
            onDismiss = { viewModel.hideTransferDialog() }
        )
    }
    if (uiState.showAssignDialog) {
        UserSelectionDialog(
            title = "Atribuir atendente",
            users = uiState.users,
            onUserSelected = { viewModel.assignToUser(it.id) },
            onDismiss = { viewModel.hideAssignDialog() }
        )
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                }
                VdAvatar(
                    name = uiState.conversation?.contact?.name ?: "?",
                    size = 38.dp,
                    fontSize = 14,
                    source = uiState.conversation?.source
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        uiState.conversation?.contact?.name ?: "Carregando…",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        buildString {
                            append(sourceLabel(uiState.conversation?.source))
                            uiState.conversation?.contact?.client?.name?.let { append(" · $it") }
                        },
                        fontSize = 11.5.sp,
                        color = VdSuccess,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = c.accent
                    )
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = { showActions = true }) {
                    Icon(Icons.Default.MoreVert, "Ações", tint = c.textPrimary)
                }
            }
            HorizontalDivider(color = c.divider, thickness = 1.dp)

            // Linha do tempo
            Box(Modifier.weight(1f)) {
                if (uiState.isLoadingMessages && thread.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = c.accent)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp)
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
                            val showDay = dayKey(item.sortDate) != dayKey(prevDate)
                            if (showDay) DaySeparator(dayLabel(item.sortDate))
                            when (item) {
                                is ThreadItem.Msg -> MessageBubble(
                                    message = item.message,
                                    isOwnMessage = item.message.sender == 1
                                )
                                is ThreadItem.Note -> NoteBubble(item.comment)
                            }
                        }
                    }
                }
            }

            // Chips de ação + composer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(top = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    ActionChip(
                        icon = Icons.Outlined.Bolt,
                        label = "Respostas rápidas",
                        active = false,
                        onClick = { showQuickReplies = true }
                    )
                    ActionChip(
                        icon = Icons.Outlined.NoteAlt,
                        label = "Nota interna",
                        active = noteMode,
                        onClick = { noteMode = !noteMode }
                    )
                    ActionChip(
                        icon = Icons.Outlined.ConfirmationNumber,
                        label = "Criar ticket",
                        active = false,
                        onClick = { toast = "Criação de ticket disponível na versão web" }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 12.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline, "Anexar",
                            tint = c.textSecondary, modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    val composerText = if (noteMode) uiState.commentText else uiState.messageText
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (noteMode) c.noteBg else c.background)
                            .border(
                                1.dp,
                                if (noteMode) c.noteBorder else c.divider,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (composerText.isEmpty()) {
                            Text(
                                if (noteMode) "Escreva uma nota interna…" else "Mensagem para o cliente…",
                                fontSize = 14.sp,
                                color = c.textSecondary
                            )
                        }
                        BasicTextField(
                            value = composerText,
                            onValueChange = {
                                if (noteMode) viewModel.onCommentTextChange(it)
                                else viewModel.onMessageTextChange(it)
                            },
                            textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                            cursorBrush = SolidColor(if (noteMode) VdWarning else c.accent),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    val canSend = composerText.isNotBlank() && !uiState.isSending
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (canSend) c.accentStrong else c.chip)
                            .border(1.dp, if (canSend) c.accentBorder else c.divider, CircleShape)
                            .clickable(enabled = canSend) {
                                if (noteMode) viewModel.sendComment()
                                else viewModel.sendMessage()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, "Enviar",
                            tint = if (canSend) c.onAccentStrong else c.textFaint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        toast?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) { VdToast(it) }
        }
    }

    // Respostas rápidas
    if (showQuickReplies) {
        ModalBottomSheet(
            onDismissRequest = { showQuickReplies = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Respostas rápidas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                DEMO_QUICK_REPLIES.forEach { reply ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.onMessageTextChange(reply.text)
                                noteMode = false
                                showQuickReplies = false
                            }
                            .padding(horizontal = 4.dp, vertical = 11.dp)
                    ) {
                        Text(
                            reply.shortcut,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.accent
                        )
                        Text(
                            reply.text,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = c.textSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    HorizontalDivider(color = c.divider, thickness = 1.dp)
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    // Ações da conversa
    if (showActions) {
        ModalBottomSheet(
            onDismissRequest = { showActions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Ações da conversa",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                VdSheetRow(Icons.Outlined.PersonAddAlt, "Atribuir atendente", iconTint = c.accent, onClick = {
                    showActions = false
                    viewModel.showAssignDialog()
                })
                VdSheetRow(Icons.Outlined.SwapHoriz, "Transferir para outro agente ou equipe", onClick = {
                    showActions = false
                    viewModel.showTransferDialog()
                })
                VdSheetRow(Icons.Outlined.NoteAlt, "Adicionar nota interna", onClick = {
                    showActions = false
                    noteMode = true
                })
                VdSheetRow(Icons.Outlined.ConfirmationNumber, "Criar ticket desta conversa", onClick = {
                    showActions = false
                    toast = "Criação de ticket disponível na versão web"
                })
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) c.noteBg else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                1.dp,
                if (active) c.noteBorder else c.divider,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            icon, null,
            tint = if (active) VdWarning else c.textSecondary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            label,
            fontSize = 12.sp,
            color = if (active) VdWarning else c.textSecondary
        )
    }
}

@Composable
private fun MessageBubble(message: Message, isOwnMessage: Boolean) {
    val c = AppTheme.colors
    val cdnUrl = BuildConfig.CDN_URL
    val shape = if (isOwnMessage) {
        RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp)
    } else {
        RoundedCornerShape(14.dp, 14.dp, 14.dp, 4.dp)
    }
    val mediaUrl = message.getMediaUrl(cdnUrl)
    val msgType = message.messageType

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (isOwnMessage) c.outBubble else c.surface)
                .border(1.dp, if (isOwnMessage) c.outBubbleBorder else c.divider, shape)
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                if (isOwnMessage && message.user != null && message.user.name.isNotBlank()) {
                    Text(
                        message.user.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.accent
                    )
                    Spacer(Modifier.height(2.dp))
                }

                when {
                    msgType == 4 && mediaUrl != null -> InlineImage(imageUrl = mediaUrl)
                    (msgType == 5 || msgType == 2) && mediaUrl != null -> AudioPlayer(audioUrl = mediaUrl)
                    msgType == 3 && mediaUrl != null && message.fileName?.let {
                        it.substringAfterLast(".", "").lowercase() in listOf("mp3", "wav", "ogg")
                    } == true -> AudioPlayer(audioUrl = mediaUrl)
                    msgType == 1 && mediaUrl != null -> VideoThumbnail(videoUrl = mediaUrl)
                    msgType == 3 && mediaUrl != null -> DocumentMessage(
                        fileName = message.fileName,
                        documentUrl = mediaUrl
                    )
                }

                if (!message.message.isNullOrEmpty()) {
                    if (msgType in listOf(4, 1)) Spacer(Modifier.height(4.dp))
                    Text(
                        message.message.replace(Regex("<[^>]*>"), ""),
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = c.textPrimary
                    )
                }

                message.createdAt?.let { dateStr ->
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            formatMessageTime(dateStr),
                            fontSize = 10.5.sp,
                            color = c.textSecondary
                        )
                        if (isOwnMessage) {
                            val read = (message.status ?: 0) >= 3
                            Icon(
                                if (read) Icons.Default.DoneAll else Icons.Default.Done,
                                contentDescription = if (read) "Lida" else "Enviada",
                                tint = if (read) c.accent else c.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteBubble(comment: Comment) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(c.noteBg)
            .border(1.dp, c.noteBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Outlined.Lock, null,
                tint = VdWarning, modifier = Modifier.size(12.dp)
            )
            Text(
                "NOTA INTERNA · ${(comment.user?.name ?: "Sistema").uppercase()}",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = VdWarning
            )
        }
        Text(
            comment.message ?: "",
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            color = c.textPrimary,
            modifier = Modifier.padding(top = 3.dp)
        )
        comment.createdAt?.let {
            Text(
                formatMessageTime(it),
                fontSize = 10.5.sp,
                color = c.textSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun UserSelectionDialog(
    title: String,
    users: List<User>,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val c = AppTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = if (searchQuery.isBlank()) users
    else users.filter {
        it.name.lowercase().contains(searchQuery.lowercase()) ||
            it.email.lowercase().contains(searchQuery.lowercase())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(title, fontWeight = FontWeight.Medium, color = c.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar usuário…", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(filteredUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user) }
                                .padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            VdAvatar(name = user.name, size = 36.dp, fontSize = 13)
                            Column {
                                Text(
                                    user.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = c.textPrimary
                                )
                                Text(user.email, fontSize = 12.sp, color = c.textSecondary)
                            }
                        }
                        HorizontalDivider(color = c.divider)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = c.textSecondary)
            }
        }
    )
}

private fun formatMessageTime(dateStr: String): String {
    val date = parseMsgDate(dateStr) ?: return dateStr
    return java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR")).format(date)
}

private fun parseMsgDate(dateStr: String?): java.util.Date? {
    if (dateStr.isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )
    for (p in patterns) {
        try {
            return java.text.SimpleDateFormat(p, java.util.Locale("pt", "BR")).parse(dateStr)
        } catch (_: Exception) {
        }
    }
    return null
}

private fun dayKey(date: java.util.Date?): String {
    if (date == null) return ""
    return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("pt", "BR")).format(date)
}

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

@Composable
private fun DaySeparator(label: String) {
    if (label.isBlank()) return
    val c = AppTheme.colors
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            label,
            fontSize = 10.5.sp,
            color = c.textFaint,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .background(c.chip, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
