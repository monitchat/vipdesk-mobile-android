package br.com.vipdesk.mobile.ui.conversations

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.vipdesk.mobile.BuildConfig
import br.com.vipdesk.mobile.data.model.*
import br.com.vipdesk.mobile.ui.components.AudioPlayer
import br.com.vipdesk.mobile.ui.components.DocumentMessage
import br.com.vipdesk.mobile.ui.components.InlineImage
import br.com.vipdesk.mobile.ui.components.VideoThumbnail
import br.com.vipdesk.mobile.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    onBack: () -> Unit,
    viewModel: ConversationDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadFile(it) }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            try {
                listState.scrollToItem(uiState.messages.size - 1)
            } catch (_: Exception) { }
        }
    }



    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionSuccess()
        }
    }

    // Transfer dialog
    if (uiState.showTransferDialog) {
        UserSelectionDialog(
            title = "Transferir Atendimento",
            users = uiState.users,
            onUserSelected = { viewModel.transferToUser(it.id) },
            onDismiss = { viewModel.hideTransferDialog() }
        )
    }

    // Assign dialog
    if (uiState.showAssignDialog) {
        UserSelectionDialog(
            title = "Atribuir Atendente",
            users = uiState.users,
            onUserSelected = { viewModel.assignToUser(it.id) },
            onDismiss = { viewModel.hideAssignDialog() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AvatarGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.conversation?.contact?.name?.let {
                                    getInitials(it)
                                } ?: "?",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = uiState.conversation?.contact?.name ?: "Carregando...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            uiState.conversation?.contact?.client?.name?.let { clientName ->
                                Text(
                                    text = clientName,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VipDeskPurple
                ),
                actions = {
                    // Transfer
                    IconButton(onClick = { viewModel.showTransferDialog() }) {
                        Icon(Icons.Default.SwapHoriz, "Transferir", tint = Color.White)
                    }
                    // Assign
                    IconButton(onClick = { viewModel.showAssignDialog() }) {
                        Icon(Icons.Default.PersonAdd, "Atribuir", tint = Color.White)
                    }
                    // More options
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Mais", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Comentarios") },
                            leadingIcon = { Icon(Icons.Default.ModeComment, null) },
                            onClick = {
                                showMenu = false
                                viewModel.setActiveTab(1)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mensagens") },
                            leadingIcon = { Icon(Icons.Default.Forum, null) },
                            onClick = {
                                showMenu = false
                                viewModel.setActiveTab(0)
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            when (uiState.activeTab) {
                0 -> MessageInputBar(
                    text = uiState.messageText,
                    onTextChange = viewModel::onMessageTextChange,
                    onSend = { viewModel.sendMessage() },
                    onAttach = { filePickerLauncher.launch("*/*") },
                    isSending = uiState.isSending
                )

                1 -> CommentInputBar(
                    text = uiState.commentText,
                    onTextChange = viewModel::onCommentTextChange,
                    onSend = { viewModel.sendComment() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SurfaceLight)
        ) {
            // Tab indicator
            TabRow(
                selectedTabIndex = uiState.activeTab,
                containerColor = Color.White,
                contentColor = VipDeskPurple,
                modifier = Modifier.height(40.dp)
            ) {
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = { Text("Mensagens", fontSize = 13.sp) }
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = { Text("Comentarios", fontSize = 13.sp) }
                )
            }

            when (uiState.activeTab) {
                0 -> {
                    // Messages
                    if (uiState.isLoadingMessages && uiState.messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VipDeskPurple)
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if (uiState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = VipDeskPurple,
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                            items(
                                items = uiState.messages,
                                key = { "${it.id}_${uiState.messages.indexOf(it)}" }
                            ) { message ->
                                MessageBubble(
                                    message = message,
                                    isOwnMessage = message.sender == 1,
                                    currentUserId = uiState.currentUserId
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Comments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.comments,
                            key = { it.id ?: it.hashCode() }
                        ) { comment ->
                            CommentBubble(comment = comment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    currentUserId: Int?
) {
    val cdnUrl = BuildConfig.CDN_URL
    val alignment = if (isOwnMessage) Arrangement.End else Arrangement.Start
    val bgColor = if (isOwnMessage) SentMessageBg else ReceivedMessageBg
    val shape = if (isOwnMessage) {
        RoundedCornerShape(12.dp, 4.dp, 12.dp, 12.dp)
    } else {
        RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp)
    }

    val mediaUrl = message.getMediaUrl(cdnUrl)
    val msgType = message.messageType

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Sender name for sent messages (show agent name)
                if (isOwnMessage && message.user != null && message.user.name.isNotBlank()) {
                    Text(
                        text = message.user.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipDeskPurple
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Media content
                when {
                    // Image (type 4)
                    msgType == 4 && mediaUrl != null -> {
                        InlineImage(imageUrl = mediaUrl)
                    }

                    // Audio (type 2 or 5, or type 3 with audio extension)
                    (msgType == 5 || msgType == 2) && mediaUrl != null -> {
                        AudioPlayer(audioUrl = mediaUrl)
                    }
                    msgType == 3 && mediaUrl != null && message.fileName?.let {
                        it.substringAfterLast(".", "").lowercase() in listOf("mp3", "wav", "ogg")
                    } == true -> {
                        AudioPlayer(audioUrl = mediaUrl)
                    }

                    // Video (type 1)
                    msgType == 1 && mediaUrl != null -> {
                        VideoThumbnail(videoUrl = mediaUrl)
                    }

                    // Document (type 3)
                    msgType == 3 && mediaUrl != null -> {
                        DocumentMessage(
                            fileName = message.fileName,
                            documentUrl = mediaUrl
                        )
                    }
                }

                // Message text (show below media if present)
                if (!message.message.isNullOrEmpty()) {
                    if (msgType in listOf(4, 1)) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = message.message.replace(Regex("<[^>]*>"), ""),
                        fontSize = 14.sp,
                        color = Color(0xFF1C1B1F)
                    )
                }

                // Timestamp
                message.createdAt?.let { dateStr ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatMessageTime(dateStr),
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentBubble(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = CommentBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.user?.name ?: "Sistema",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipDeskPurple
                )
                comment.createdAt?.let {
                    Text(
                        text = formatMessageTime(it),
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.message ?: "",
                fontSize = 13.sp,
                color = Color(0xFF1C1B1F)
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit,
    isSending: Boolean
) {
    Surface(
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Attach button
            IconButton(
                onClick = onAttach,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Anexar",
                    tint = TextSecondary
                )
            }

            // Text input
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Digite uma mensagem...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VipDeskPurple,
                    unfocusedBorderColor = CardBorder
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isSending) VipDeskPurple
                        else CardBorder
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = CommentBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            Icon(
                Icons.Default.ModeComment,
                contentDescription = null,
                tint = VipDeskOrange,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Adicionar comentario...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 120.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VipDeskOrange,
                    unfocusedBorderColor = CardBorder
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) VipDeskOrange else CardBorder)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar comentario",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
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
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = if (searchQuery.isBlank()) users
    else users.filter {
        it.name.lowercase().contains(searchQuery.lowercase()) ||
                it.email.lowercase().contains(searchQuery.lowercase())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar usuario...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(filteredUsers) { user ->
                        ListItem(
                            headlineContent = {
                                Text(user.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            },
                            supportingContent = {
                                Text(user.email, fontSize = 12.sp, color = TextSecondary)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(VipDeskPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUserSelected(user) }
                        )
                        HorizontalDivider(color = DividerColor)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun getInitials(fullName: String): String {
    return fullName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
}

private fun formatMessageTime(dateStr: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale("pt", "BR"))
        val date = sdf.parse(dateStr)
        val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR"))
        date?.let { timeFmt.format(it) } ?: dateStr
    } catch (_: Exception) {
        try {
            // Try ISO format
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale("pt", "BR"))
            val date = sdf.parse(dateStr)
            val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("pt", "BR"))
            date?.let { timeFmt.format(it) } ?: dateStr
        } catch (_: Exception) {
            dateStr
        }
    }
}
